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
