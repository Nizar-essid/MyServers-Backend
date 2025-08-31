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
@Table(name = "user_group_assignments")
@NoArgsConstructor
@AllArgsConstructor
public class UserGroupAssignment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "user_group_id", nullable = false)
    private UserGroup userGroup;

    @Column(name = "assigned_date", nullable = false)
    private Date assignedDate;

    @ManyToOne
    @JoinColumn(name = "assigned_by_id")
    private Admin assignedBy;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}
