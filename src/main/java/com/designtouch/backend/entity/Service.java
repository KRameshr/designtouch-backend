package com.designtouch.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "service")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Service {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(length = 100)
    private String icon;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    // Maps to Postgres TEXT[] — bullet point features list
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(columnDefinition = "text[]")
    private List<String> features;

    @Column(name = "color_theme", length = 30)
    private String colorTheme;

    // Used for the "LEARN MORE" -> /services/{slug} detail page
    @Column(nullable = false, unique = true, length = 150)
    private String slug;

    @Column(name = "order_number", length = 10)
    private String orderNumber;

    @Column(name = "display_order", nullable = false)
    @Builder.Default
    private Integer displayOrder = 0;
}
