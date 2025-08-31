package com.myservers.backend.users.entities;

import com.myservers.backend.security.auth.entities.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Getter
@Setter
@Builder
@Entity
@Table(name = "user_balances")
@NoArgsConstructor
@AllArgsConstructor
public class UserBalance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "balance", nullable = false)
    private Double balance = 0.0;

    @Column(name = "total_deposited", nullable = false)
    private Double totalDeposited = 0.0;

    @Column(name = "total_spent", nullable = false)
    private Double totalSpent = 0.0;

    @Column(name = "last_updated", nullable = false)
    private Date lastUpdated;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}
