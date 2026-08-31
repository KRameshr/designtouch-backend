package com.designtouch.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Singleton table — exactly one row. Reused across the Footer (every page)
 * and the Contact page's "GET IN TOUCH" cards, so editing it once in the
 * admin panel updates the whole site.
 */
@Entity
@Table(name = "company_info")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyInfo {

    @Id
    @Builder.Default
    private Short id = 1;

    @Column(nullable = false, length = 150)
    private String email;

    @Column(nullable = false, length = 30)
    private String phone;

    @Column(name = "whatsapp_number", length = 30)
    private String whatsappNumber;

    @Column(length = 250)
    private String address;

    @Column(name = "working_hours", length = 150)
    private String workingHours;

    @Column(name = "map_embed_url", columnDefinition = "TEXT")
    private String mapEmbedUrl;
}
