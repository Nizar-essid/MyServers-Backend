package com.myservers.backend.users.entities;

import com.myservers.backend.security.auth.entities.Admin;
import com.myservers.backend.security.auth.entities.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Getter
@Setter
@Builder
@Entity
@Table(name = "balance_change_history")
@NoArgsConstructor
@AllArgsConstructor
public class BalanceChangeHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "admin_id", nullable = true)
    private Admin admin;

    @Column(name = "amount", nullable = false)
    private Double amount;

    @Column(name = "previous_balance", nullable = false)
    private Double previousBalance;

    @Column(name = "new_balance", nullable = false)
    private Double newBalance;

    @Column(name = "change_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private ChangeType changeType;

    @Column(name = "payment_status", nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "cancellation_reason", columnDefinition = "TEXT")
    private String cancellationReason;

    @Column(name = "change_date", nullable = false)
    private Date changeDate;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    public enum ChangeType {
        ADD("Ajout"),
        SET("Définition");

        private final String displayName;

        ChangeType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum PaymentStatus {
        PAID("Payé"),
        UNPAID("Non payé"),
        CANCELLED("Annulé");

        private final String displayName;

        PaymentStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}
