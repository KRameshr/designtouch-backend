package com.designtouch.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "newsletter_subscriber")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewsletterSubscriber {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "subscribed_at", nullable = false, updatable = false)
    private OffsetDateTime subscribedAt;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "confirmation_sent", nullable = false)
    @Builder.Default
    private Boolean confirmationSent = false;

    @Column(name = "confirmation_sent_at")
    private OffsetDateTime confirmationSentAt;

    @PrePersist
    protected void onCreate() {
        if (subscribedAt == null) {
            subscribedAt = OffsetDateTime.now();
        }
        if (confirmationSent == null) {
            confirmationSent = false;
        }
    }
}
