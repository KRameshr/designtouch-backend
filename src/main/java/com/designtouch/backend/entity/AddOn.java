package com.designtouch.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Table(name = "add_on")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddOn {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 100)
    private String icon;

    @Column(name = "price_label", nullable = false, length = 50)
    private String priceLabel;

    @Column(name = "display_order", nullable = false)
    @Builder.Default
    private Integer displayOrder = 0;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "custom_icon_url", columnDefinition = "TEXT")
    private String customIconUrl;

    @Column(name = "custom_icon_public_id", columnDefinition = "TEXT")
    private String customIconPublicId;
}
