package com.designtouch.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Service for sending transactional emails via Resend API (https://resend.com).
 * Safely handles missing credentials, network errors, and provider failures
 * without interrupting database transactions or user flows.
 */
@Service
@Slf4j
public class EmailService {

    private static final String RESEND_API_URL = "https://api.resend.com/emails";

    private final String apiKey;
    private final String fromEmail;
    private final boolean enabled;
    private final ObjectMapper objectMapper;
    private volatile HttpClient httpClient;
    private volatile String lastErrorDescription = "";

    /**
     * Spring dependency-injection constructor.
     * Explicitly annotated with @Autowired so Spring identifies this constructor
     * when multiple constructors are defined on the class.
     */
    @Autowired
    public EmailService(
            @Value("${resend.api-key:}") String apiKey,
            @Value("${resend.from-email:DesignTouch+ <onboarding@resend.dev>}") String fromEmail,
            @Value("${resend.enabled:true}") boolean enabled,
            ObjectMapper objectMapper
    ) {
        this(apiKey, fromEmail, enabled, null, objectMapper);
    }

    /**
     * Testing constructor allowing custom HttpClient and ObjectMapper injection.
     */
    public EmailService(
            String apiKey,
            String fromEmail,
            boolean enabled,
            HttpClient httpClient,
            ObjectMapper objectMapper
    ) {
        this.apiKey = apiKey != null ? apiKey.trim() : "";
        this.fromEmail = fromEmail != null ? fromEmail.trim() : "DesignTouch+ <onboarding@resend.dev>";
        this.enabled = enabled;
        this.httpClient = httpClient;
        this.objectMapper = objectMapper != null ? objectMapper : new ObjectMapper();
    }

    /**
     * Lazily creates the HttpClient only when an actual email dispatch is performed.
     * Prevents eager socket/loopback initialization during Spring bean creation.
     */
    private HttpClient getOrCreateHttpClient() {
        if (httpClient == null) {
            synchronized (this) {
                if (httpClient == null) {
                    httpClient = HttpClient.newBuilder()
                            .connectTimeout(Duration.ofSeconds(5))
                            .build();
                }
            }
        }
        return httpClient;
    }

    /**
     * Sends a welcome email to a new subscriber via Resend.
     *
     * @param toEmail Normalized email address of the subscriber
     * @return true if the email was successfully accepted by Resend, false otherwise.
     */
    public boolean sendWelcomeEmail(String toEmail) {
        if (!enabled) {
            log.info("[EmailService] Email delivery is disabled by configuration (resend.enabled=false). Skipping for {}", toEmail);
            return false;
        }

        if (apiKey.isEmpty() || isPlaceholderKey(apiKey)) {
            log.info("[EmailService] Resend API key is not configured or placeholder detected. Skipping welcome email delivery for {}", toEmail);
            return false;
        }

        try {
            String subject = "Welcome to DesignTouch+ | Creative Video & Design Insights";
            String htmlContent = buildWelcomeEmailHtml(toEmail);

            Map<String, Object> payload = Map.of(
                    "from", fromEmail,
                    "to", List.of(toEmail),
                    "subject", subject,
                    "html", htmlContent
            );

            String requestBody = objectMapper.writeValueAsString(payload);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(RESEND_API_URL))
                    .timeout(Duration.ofSeconds(8))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = getOrCreateHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                String emailId = "N/A";
                try {
                    Map<?, ?> respMap = objectMapper.readValue(response.body(), Map.class);
                    if (respMap != null && respMap.get("id") != null) {
                        emailId = respMap.get("id").toString();
                    }
                } catch (Exception ignored) {
                }
                log.info("[EmailService] Welcome email delivered successfully to {} via Resend (status: {}, emailId: {})",
                        toEmail, response.statusCode(), emailId);
                return true;
            } else {
                log.error("[EmailService] Resend API rejected email for {}. HTTP status: {}, response: {}",
                        toEmail, response.statusCode(), response.body());
                return false;
            }
        } catch (Exception ex) {
            log.error("[EmailService] Failed to send welcome email to {} via Resend: {}", toEmail, ex.getMessage(), ex);
            return false;
        }
    }

    /**
     * Sends a notification email for a new website contact form enquiry to the business inbox.
     * Sets the visitor's email address as the Reply-To header so direct replies reach the visitor.
     *
     * @param recipientEmail Target business recipient (e.g. designtouchplus@gmail.com)
     * @param visitorName    Name of the visitor
     * @param visitorEmail   Email of the visitor (Reply-To)
     * @param company        Company name (optional)
     * @param subject        Enquiry subject line (optional)
     * @param message        Full project inquiry description
     * @param submittedAt    Submission timestamp
     * @return true if accepted by provider, false otherwise
     */
    public boolean sendContactEnquiryEmail(
            String recipientEmail,
            String visitorName,
            String visitorEmail,
            String company,
            String subject,
            String message,
            java.time.OffsetDateTime submittedAt
    ) {
        if (!enabled) {
            lastErrorDescription = "Email delivery is disabled by configuration (resend.enabled=false).";
            log.info("[EmailService] {}", lastErrorDescription);
            return false;
        }

        if (apiKey.isEmpty() || isPlaceholderKey(apiKey)) {
            lastErrorDescription = "Resend API key is not configured or placeholder detected.";
            log.info("[EmailService] {}", lastErrorDescription);
            return false;
        }

        try {
            String targetRecipient = (recipientEmail != null && !recipientEmail.isBlank())
                    ? recipientEmail.trim()
                    : "designtouchplus@gmail.com";

            String emailSubject = (subject != null && !subject.isBlank())
                    ? "New Website Enquiry — " + subject.trim()
                    : "New Website Enquiry — " + (visitorName != null && !visitorName.isBlank() ? visitorName.trim() : "DesignTouch+");

            java.time.OffsetDateTime timestamp = submittedAt != null ? submittedAt : java.time.OffsetDateTime.now();
            String htmlContent = buildContactEnquiryEmailHtml(visitorName, visitorEmail, company, subject, message, timestamp);
            String textContent = buildContactEnquiryEmailText(visitorName, visitorEmail, company, subject, message, timestamp);

            Map<String, Object> payload = new java.util.LinkedHashMap<>();
            payload.put("from", fromEmail);
            payload.put("to", List.of(targetRecipient));
            if (visitorEmail != null && !visitorEmail.isBlank()) {
                payload.put("reply_to", visitorEmail.trim());
            }
            payload.put("subject", emailSubject);
            payload.put("html", htmlContent);
            payload.put("text", textContent);

            String requestBody = objectMapper.writeValueAsString(payload);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(RESEND_API_URL))
                    .timeout(Duration.ofSeconds(10))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = getOrCreateHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                String emailId = "N/A";
                try {
                    Map<?, ?> respMap = objectMapper.readValue(response.body(), Map.class);
                    if (respMap != null && respMap.get("id") != null) {
                        emailId = respMap.get("id").toString();
                    }
                } catch (Exception ignored) {
                }
                lastErrorDescription = "";
                log.info("[EmailService] Contact enquiry email delivered successfully for visitor {} to recipient {} via Resend (status: {}, emailId: {})",
                        visitorEmail, targetRecipient, response.statusCode(), emailId);
                return true;
            } else {
                lastErrorDescription = "Resend API rejected contact notification (HTTP " + response.statusCode() + ")";
                log.warn("[EmailService] Resend rejected contact notification (HTTP {})", response.statusCode());
                return false;
            }
        } catch (Exception ex) {
            lastErrorDescription = "Contact notification failed (" + ex.getClass().getSimpleName() + ")";
            log.warn("[EmailService] Contact notification request failed (error type: {})", ex.getClass().getSimpleName());
            return false;
        }
    }

    public String getLastErrorDescription() {
        return lastErrorDescription;
    }

    public static String escapeHtml(String input) {
        if (input == null) return "";
        StringBuilder sb = new StringBuilder(input.length() + 16);
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            switch (c) {
                case '&' -> sb.append("&amp;");
                case '<' -> sb.append("&lt;");
                case '>' -> sb.append("&gt;");
                case '"' -> sb.append("&quot;");
                case '\'' -> sb.append("&#39;");
                default -> sb.append(c);
            }
        }
        return sb.toString();
    }

    public static String formatSubmissionTime(java.time.OffsetDateTime timestamp) {
        if (timestamp == null) {
            timestamp = java.time.OffsetDateTime.now();
        }
        try {
            java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy, hh:mm a (z)");
            java.time.ZonedDateTime istTime = timestamp.atZoneSameInstant(java.time.ZoneId.of("Asia/Kolkata"));
            return istTime.format(dtf) + " [" + timestamp.format(java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME) + "]";
        } catch (Exception e) {
            return timestamp.toString();
        }
    }

    private String buildContactEnquiryEmailHtml(
            String visitorName,
            String visitorEmail,
            String company,
            String subject,
            String message,
            java.time.OffsetDateTime timestamp
    ) {
        String safeName = escapeHtml(visitorName != null ? visitorName.trim() : "Not provided");
        String safeEmail = escapeHtml(visitorEmail != null ? visitorEmail.trim() : "Not provided");
        String safeCompany = (company != null && !company.isBlank())
                ? escapeHtml(company.trim())
                : "<span style=\"color: #737373; font-style: italic;\">None / Individual</span>";
        String safeSubject = (subject != null && !subject.isBlank())
                ? escapeHtml(subject.trim())
                : "<span style=\"color: #737373; font-style: italic;\">General Inquiry</span>";
        String safeMessage = escapeHtml(message != null ? message.trim() : "")
                .replace("\r\n", "<br/>")
                .replace("\n", "<br/>");
        String formattedTime = escapeHtml(formatSubmissionTime(timestamp));

        return """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <title>New Website Enquiry</title>
                    <style>
                        body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; background-color: #0a0a0a; color: #f5f5f5; margin: 0; padding: 32px 16px; }
                        .container { max-width: 620px; margin: 0 auto; background-color: #121212; border: 1px solid rgba(255,255,255,0.08); padding: 32px; border-radius: 4px; box-shadow: 0 4px 24px rgba(0,0,0,0.4); }
                        .brand-bar { display: flex; align-items: center; justify-content: space-between; border-bottom: 1px solid rgba(255,255,255,0.08); padding-bottom: 18px; margin-bottom: 24px; }
                        .brand { font-size: 18px; font-weight: 900; letter-spacing: -0.5px; color: #ffffff; text-transform: uppercase; }
                        .brand-red { color: #ef4444; }
                        .badge { display: inline-block; font-size: 10px; font-weight: 700; text-transform: uppercase; letter-spacing: 1px; color: #ef4444; background: rgba(239,68,68,0.1); border: 1px solid rgba(239,68,68,0.3); padding: 3px 8px; border-radius: 2px; }
                        h1 { font-size: 20px; font-weight: 800; color: #ffffff; margin: 0 0 10px; text-transform: uppercase; letter-spacing: 0.5px; }
                        .subtitle { font-size: 13px; color: #a3a3a3; margin: 0 0 24px; line-height: 1.5; }
                        .details-table { width: 100%; border-collapse: collapse; margin-bottom: 24px; }
                        .details-table td { padding: 9px 12px; font-size: 13px; border-bottom: 1px solid rgba(255,255,255,0.05); }
                        .details-table .label { width: 140px; color: #888888; font-weight: 600; text-transform: uppercase; font-size: 11px; letter-spacing: 0.8px; }
                        .details-table .value { color: #ffffff; }
                        .message-section { margin-top: 24px; }
                        .message-label { font-size: 11px; font-weight: 700; text-transform: uppercase; letter-spacing: 0.8px; color: #888888; margin-bottom: 8px; }
                        .message-box { background-color: #181818; border-left: 3px solid #ef4444; padding: 16px 18px; color: #f5f5f5; font-size: 13px; line-height: 1.6; border-radius: 0 3px 3px 0; }
                        .action-area { margin-top: 28px; padding-top: 20px; border-top: 1px solid rgba(255,255,255,0.08); text-align: left; }
                        .btn-reply { display: inline-block; background-color: #ef4444; color: #ffffff !important; text-decoration: none; font-size: 12px; font-weight: 700; text-transform: uppercase; letter-spacing: 1px; padding: 12px 22px; border-radius: 2px; }
                        .footer { margin-top: 24px; font-size: 11px; color: #666666; line-height: 1.5; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="brand-bar">
                            <div class="brand">DESIGN<span class="brand-red">TOUCH+</span></div>
                            <span class="badge">Website Enquiry</span>
                        </div>
                        <h1>New Project Inquiry</h1>
                        <p class="subtitle">A visitor submitted an inquiry through the DesignTouch+ contact form.</p>
                        <table class="details-table">
                            <tr>
                                <td class="label">Visitor Name</td>
                                <td class="value"><strong>{{VISITOR_NAME}}</strong></td>
                            </tr>
                            <tr>
                                <td class="label">Email Address</td>
                                <td class="value"><a href="mailto:{{VISITOR_EMAIL}}" style="color: #ef4444; text-decoration: none;">{{VISITOR_EMAIL}}</a></td>
                            </tr>
                            <tr>
                                <td class="label">Company</td>
                                <td class="value">{{COMPANY}}</td>
                            </tr>
                            <tr>
                                <td class="label">Subject</td>
                                <td class="value">{{SUBJECT}}</td>
                            </tr>
                            <tr>
                                <td class="label">Submission Time</td>
                                <td class="value">{{SUBMISSION_TIME}}</td>
                            </tr>
                        </table>
                        <div class="message-section">
                            <div class="message-label">Project Description / Message</div>
                            <div class="message-box">{{MESSAGE}}</div>
                        </div>
                        <div class="action-area">
                            <a href="mailto:{{VISITOR_EMAIL}}" class="btn-reply">Reply to {{VISITOR_NAME}} &rarr;</a>
                        </div>
                        <div class="footer">
                            Sent automatically from the public Contact page on designtouchplus.com.<br/>
                            Reply directly to this email to reach <strong>{{VISITOR_EMAIL}}</strong>.
                        </div>
                    </div>
                </body>
                </html>
                """
                .replace("{{VISITOR_NAME}}", safeName)
                .replace("{{VISITOR_EMAIL}}", safeEmail)
                .replace("{{COMPANY}}", safeCompany)
                .replace("{{SUBJECT}}", safeSubject)
                .replace("{{SUBMISSION_TIME}}", formattedTime)
                .replace("{{MESSAGE}}", safeMessage);
    }

    private String buildContactEnquiryEmailText(
            String visitorName,
            String visitorEmail,
            String company,
            String subject,
            String message,
            java.time.OffsetDateTime timestamp
    ) {
        String companyStr = (company != null && !company.isBlank()) ? company.trim() : "Not specified";
        String subjectStr = (subject != null && !subject.isBlank()) ? subject.trim() : "General Inquiry";
        String timeStr = formatSubmissionTime(timestamp);

        return String.format("""
                NEW WEBSITE ENQUIRY — DESIGNTOUCH+
                ============================================================

                Visitor Name:    %s
                Visitor Email:   %s
                Company:         %s
                Subject:         %s
                Submission Time: %s

                PROJECT DESCRIPTION / MESSAGE:
                ------------------------------------------------------------
                %s
                ------------------------------------------------------------

                Reply directly to this email to contact %s (%s).
                """,
                visitorName != null ? visitorName.trim() : "Not provided",
                visitorEmail != null ? visitorEmail.trim() : "Not provided",
                companyStr,
                subjectStr,
                timeStr,
                message != null ? message.trim() : "",
                visitorName != null ? visitorName.trim() : "the visitor",
                visitorEmail != null ? visitorEmail.trim() : "Not provided"
        );
    }

    private boolean isPlaceholderKey(String key) {
        return key.equalsIgnoreCase("your_resend_api_key")
                || key.equalsIgnoreCase("re_placeholder")
                || key.equalsIgnoreCase("re_test")
                || key.equalsIgnoreCase("re_test_key_12345")
                || key.startsWith("replace_with_");
    }

    private String buildWelcomeEmailHtml(String subscriberEmail) {
        return """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <title>Welcome to DesignTouch+</title>
                    <style>
                        body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; background-color: #0d0d0d; color: #f5f5f5; margin: 0; padding: 40px 20px; }
                        .container { max-width: 580px; margin: 0 auto; background-color: #141414; border: 1px solid rgba(255,255,255,0.1); padding: 36px; border-radius: 0; }
                        .brand { font-size: 20px; font-weight: 900; letter-spacing: -0.5px; color: #ffffff; margin-bottom: 24px; }
                        .brand-red { color: #e11d48; }
                        h1 { font-size: 22px; font-weight: 700; color: #ffffff; margin: 0 0 16px; line-height: 1.3; }
                        p { font-size: 14px; line-height: 1.6; color: #a3a3a3; margin: 0 0 18px; }
                        .highlight-box { background: rgba(225, 29, 72, 0.08); border-left: 3px solid #e11d48; padding: 14px 18px; margin: 20px 0; }
                        .btn { display: inline-block; background-color: #e11d48; color: #ffffff !important; text-decoration: none; font-size: 12px; font-weight: 700; text-transform: uppercase; letter-spacing: 1px; padding: 12px 24px; margin-top: 10px; border-radius: 0; }
                        .footer { margin-top: 36px; padding-top: 20px; border-top: 1px solid rgba(255,255,255,0.06); font-size: 11px; color: #737373; line-height: 1.5; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="brand">DESIGN<span class="brand-red">TOUCH+</span></div>
                        <h1>Welcome to the Creative Circle</h1>
                        <p>Thank you for subscribing to our newsletter! You're officially in the loop for the latest video editing tips, motion graphics trends, and creative strategies from DesignTouch+.</p>
                        <div class="highlight-box">
                            <p style="margin: 0; color: #f5f5f5; font-size: 13px;"><strong>What you can expect:</strong> Weekly actionable breakdowns on retention editing, thumbnail psychology, color grading, and creative agency workflows.</p>
                        </div>
                        <p>In the meantime, feel free to explore our recent articles and case studies on our blog.</p>
                        <a href="https://designtouchplus.com/blog" class="btn">Explore Articles &rarr;</a>
                        <div class="footer">
                            <p>You received this email because %s subscribed to the DesignTouch+ newsletter.<br/>
                            Zero spam. Unsubscribe at any time from future emails.</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(subscriberEmail);
    }
}
