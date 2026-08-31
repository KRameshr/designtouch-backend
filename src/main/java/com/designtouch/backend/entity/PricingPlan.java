package com.designtouch.backend.entity;

import com.designtouch.backend.entity.enums.PriceType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "pricing_plan")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PricingPlan {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(name = "plan_name", nullable = false, length = 100)
    private String planName;

    @Column(length = 100)
    private String icon;

    @Column(columnDefinition = "TEXT")
    private String description;

    // Maps to Postgres native enum `price_type`
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "price_type", nullable = false, columnDefinition = "price_type")
    private PriceType priceType;

    // Null = "CUSTOM" pricing (Enterprise plan) — check for null in the frontend/DTO layer
    @Column(precision = 10, scale = 2)
    private BigDecimal price;

    @Column(length = 5)
    @Builder.Default
    private String currency = "₹";

    @Column(length = 30)
    private String unit;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(columnDefinition = "text[]")
    private List<String> features;

    @Column(name = "is_most_popular", nullable = false)
    @Builder.Default
    private Boolean isMostPopular = false;

    @Column(name = "cta_text", length = 50)
    private String ctaText;

    @Column(name = "cta_link", length = 200)
    private String ctaLink;

    @Column(name = "display_order", nullable = false)
    @Builder.Default
    private Integer displayOrder = 0;
}
