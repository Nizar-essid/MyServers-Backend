package com.myservers.backend.servers.entities;

import com.myservers.backend.security.auth.entities.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Getter
@Setter
@Builder
@Entity
@Table(name = "subscriptions")
@NoArgsConstructor
@AllArgsConstructor
public class Subscription {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id_subscription", nullable = false)
    private Long idSubscription;

    private Date date_creation;

    @Column(name = "date_latest_update")
    private Date dateLatestUpdate;

    @Enumerated(EnumType.STRING)
    private SubscrptionState state;

    @Column(name = "verification_code", nullable = false)
    private String verificationCode;

    @ManyToOne
    @JoinColumn(name = "purchaser_id_user")
    private User purchaser;

    @ManyToOne
    @JoinColumn(name = "related_code_id")
    private Code relatedCode;

    // New field: price after discount (nullable if no discount)
    @Column(name = "price_after_discount")
    private Double priceAfterDiscount;

    // New field: discount percentage applied (nullable if no discount)
    @Column(name = "discount_percentage")
    private Double discountPercentage;
}
