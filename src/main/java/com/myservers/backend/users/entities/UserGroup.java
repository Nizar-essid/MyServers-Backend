package com.myservers.backend.users.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.myservers.backend.security.auth.entities.Admin;
import com.myservers.backend.security.auth.entities.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@Builder
@Entity
@Table(name = "user_groups")
@NoArgsConstructor
@AllArgsConstructor
public class UserGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "discount_percentage")
    private Double discountPercentage;

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

    @JsonIgnore
    @ManyToMany(mappedBy = "userGroups", fetch = FetchType.LAZY)
    private List<User> users;

    @OneToMany(mappedBy = "userGroup", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<GroupPrice> groupPrices;
}
