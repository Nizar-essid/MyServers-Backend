package com.myservers.backend.shop.entities;

import com.myservers.backend.security.auth.entities.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "quote_requests")
public class QuoteRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private String contactName;

    @Column(nullable = false)
    private String contactEmail;

    @Column
    private String contactPhone;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private QuoteRequestStatus status = QuoteRequestStatus.PENDING;

    @Column(nullable = false)
    private Date dateCreation;

    @Column
    private Date dateProcessed;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processed_by_id")
    private com.myservers.backend.security.auth.entities.Admin processedBy;

    @Column(columnDefinition = "TEXT")
    private String adminNotes;
}

