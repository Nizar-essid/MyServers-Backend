package com.myservers.backend.users.entities;

import com.myservers.backend.security.auth.entities.Admin;
import com.myservers.backend.servers.entities.Server;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Getter
@Setter
@Builder
@Entity
@Table(name = "group_server_discounts")
@NoArgsConstructor
@AllArgsConstructor
public class GroupServerDiscount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_group_id", nullable = false)
    private UserGroup userGroup;

    @ManyToOne
    @JoinColumn(name = "server_id", nullable = false)
    private Server server;

    @Column(name = "discount_percentage", nullable = false)
    private Double discountPercentage; // 0-100

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
