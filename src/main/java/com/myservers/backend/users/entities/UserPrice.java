package com.myservers.backend.users.entities;

import com.myservers.backend.servers.entities.Server;
import com.myservers.backend.security.auth.entities.Admin;
import com.myservers.backend.security.auth.entities.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Getter
@Setter
@Builder
@Entity
@Table(name = "user_prices")
@NoArgsConstructor
@AllArgsConstructor
public class UserPrice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "server_id", nullable = false)
    private Server server;

    @Column(name = "price", nullable = false)
    private Double price;

    @Column(name = "duration_days", nullable = false)
    private Integer durationDays;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "date_creation", nullable = false)
    private Date dateCreation;

    @Column(name = "latest_update")
    private Date latestUpdate;

    @ManyToOne
    @JoinColumn(name = "created_by_id")
    private Admin createdBy;

    @ManyToOne
    @JoinColumn(name = "updated_by_id")
    private Admin updatedBy;
}
