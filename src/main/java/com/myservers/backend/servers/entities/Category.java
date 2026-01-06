package com.myservers.backend.servers.entities;

import com.myservers.backend.security.auth.entities.Admin;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "categories")
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Category> children;

    @Column(name = "date_creation", nullable = false)
    private Date dateCreation;

    @Column(name = "latest_update")
    private Date latestUpdate;

    @ManyToOne
    @JoinColumn(name = "created_by_id_user")
    private Admin createdBy;

    @ManyToOne
    @JoinColumn(name = "updated_by_id_user")
    private Admin updatedBy;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;
}

