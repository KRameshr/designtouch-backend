package com.designtouch.backend.config;

import com.designtouch.backend.entity.*;
import com.designtouch.backend.entity.enums.*;
import com.designtouch.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * Inserts a handful of sample rows so you have something to look at while
 * building the frontend. Only runs on the "dev" profile (never in production —
 * see application-dev.properties) and only if each table is currently empty,
 * so re-running the app doesn't create duplicates.
 */
@Component
@Profile("dev")
@RequiredArgsConstructor
public class DevDataSeeder implements CommandLineRunner {

    private final TeamMemberRepository teamMemberRepository;
    private final ClientRepository clientRepository;
    private final TestimonialRepository testimonialRepository;
    private final StatItemRepository statItemRepository;
    private final ServiceRepository serviceRepository;
    private final FaqRepository faqRepository;
    private final PricingPlanRepository pricingPlanRepository;
    private final AboutContentRepository aboutContentRepository;
    private final CompanyInfoRepository companyInfoRepository;

    @Override
    public void run(String... args) {
        seedTeamMembers();
        seedClients();
        seedTestimonials();
        seedStats();
        seedServices();
        seedFaqs();
        seedPricingPlans();
        seedAboutContent();
        seedCompanyInfo();
    }

    private void seedTeamMembers() {
        if (teamMemberRepository.count() > 0) return;
        teamMemberRepository.saveAll(List.of(
                TeamMember.builder().name("Rohit Sharma").role("Founder & Creative Director").displayOrder(1).build(),
                TeamMember.builder().name("Arjun Verma").role("Head of Video Production").displayOrder(2).build(),
                TeamMember.builder().name("Pooja Nair").role("Motion Graphics Designer").displayOrder(3).build()
        ));
    }

    private void seedClients() {
        if (clientRepository.count() > 0) return;
        clientRepository.saveAll(List.of(
                Client.builder().clientName("Groww").logoUrl("https://placehold.co/120x40?text=Groww").displayOrder(1).build(),
                Client.builder().clientName("boAt").logoUrl("https://placehold.co/120x40?text=boAt").displayOrder(2).build(),
                Client.builder().clientName("upGrad").logoUrl("https://placehold.co/120x40?text=upGrad").displayOrder(3).build()
        ));
    }

    private void seedTestimonials() {
        if (testimonialRepository.count() > 0) return;
        testimonialRepository.save(Testimonial.builder()
                .quote("DesignTouch+ transformed our raw footage into a compelling story. Their attention to detail and creativity is unmatched.")
                .authorName("Rohit Sharma")
                .authorRole("Marketing Head")
                .authorCompany("Groww")
                .rating((short) 5)
                .displayOrder(1)
                .build());
    }

    private void seedStats() {
        if (statItemRepository.count() > 0) return;
        statItemRepository.saveAll(List.of(
                StatItem.builder().label("Projects Completed").value("80+").suffix("+").description("Projects Completed").displayOrder(1).build(),
                StatItem.builder().label("Happy Clients").value("4+").suffix("+").description("Happy Clients").displayOrder(2).build(),
                StatItem.builder().label("Years Experience").value("10+").suffix("+").description("Years Experience").displayOrder(3).build(),
                StatItem.builder().label("Countries Served").value("10+").suffix("+").description("Countries Served").displayOrder(4).build()
        ));
    }

    private void seedServices() {
        if (serviceRepository.count() > 0) return;
        serviceRepository.save(Service.builder()
                .icon("video")
                .title("Video Editing")
                .description("Engaging edits for YouTube, social media, ads, podcasts and more.")
                .features(List.of("Long-form Editing", "Short-form / Reels", "Podcast Editing", "Color Grading"))
                .colorTheme("red")
                .slug("video-editing")
                .orderNumber("01")
                .displayOrder(1)
                .build());
    }

    private void seedFaqs() {
        if (faqRepository.count() > 0) return;
        faqRepository.saveAll(List.of(
                Faq.builder().question("How soon can you start my project?").answer("Usually within 2-3 business days of approval.").category(FaqCategory.GENERAL).displayOrder(1).build(),
                Faq.builder().question("What information do you need to get started?").answer("Raw footage, brand guidelines, and a brief on your goals.").category(FaqCategory.PRICING).displayOrder(1).build()
        ));
    }

    private void seedPricingPlans() {
        if (pricingPlanRepository.count() > 0) return;
        pricingPlanRepository.saveAll(List.of(
                PricingPlan.builder()
                        .planName("Basic").icon("paper-plane")
                        .description("Perfect for creators and small projects.")
                        .priceType(PriceType.ONE_TIME).price(new BigDecimal("2999")).unit("/video")
                        .features(List.of("Up to 5 minutes runtime", "Basic Editing", "Color Correction", "2 Revisions", "3 Days Delivery"))
                        .ctaText("GET STARTED").displayOrder(1)
                        .build(),
                PricingPlan.builder()
                        .planName("Standard").icon("star")
                        .description("Great for businesses and regular content.")
                        .priceType(PriceType.ONE_TIME).price(new BigDecimal("6999")).unit("/video")
                        .features(List.of("Up to 10 minutes runtime", "Advanced Editing", "Motion Graphics", "Unlimited Revisions", "2 Days Delivery"))
                        .isMostPopular(true).ctaText("GET STARTED").displayOrder(2)
                        .build()
        ));
    }

    private void seedAboutContent() {
        if (aboutContentRepository.findById((short) 1).isPresent()) return;
        aboutContentRepository.save(AboutContent.builder()
                .heading("WE ARE DESIGNTOUCH+")
                .subheading("We are a team of passionate storytellers, editors and creators who turn ideas into powerful visual experiences.")
                .storyParagraph1("DesignTouch+ was born out of a love for storytelling and a passion for visual creativity.")
                .storyParagraph2("We believe every brand has a story worth telling, and our job is to tell it the right way.")
                .founderName("Founder, DesignTouch+")
                .build());
    }

    private void seedCompanyInfo() {
        if (companyInfoRepository.findById((short) 1).isPresent()) return;
        companyInfoRepository.save(CompanyInfo.builder()
                .email("hello@designtouchplus.com")
                .phone("+91 12345 67890")
                .whatsappNumber("+911234567890")
                .address("India · Remote Worldwide")
                .workingHours("Monday - Saturday, 10:00 AM - 7:00 PM (IST)")
                .build());
    }
}
