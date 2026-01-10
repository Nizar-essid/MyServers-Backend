package com.myservers.backend.shop.entities;

import com.myservers.backend.users.entities.UserGroup;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "shop_access")
public class ShopAccess {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_group_id", nullable = false, unique = true)
    private UserGroup userGroup;

    @Column(nullable = false)
    private Boolean hasAccess = true;

    @Column(nullable = false)
    private Date dateCreation;

    private Date latestUpdate;
}

