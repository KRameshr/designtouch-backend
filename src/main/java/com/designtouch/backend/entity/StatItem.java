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
@Table(name = "stat_item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatItem {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(nullable = false, length = 50)
    private String label;

    @Column(nullable = false)
    private Integer value;

    @Column(length = 10)
    private String suffix;

    @Column(nullable = false, length = 150)
    private String description;

    @Column(length = 100)
    private String icon;

    @Column(name = "display_order", nullable = false)
    @Builder.Default
    private Integer displayOrder = 0;
}
