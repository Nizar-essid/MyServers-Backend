package com.myservers.backend.notifications.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.myservers.backend.notifications.enums.NotificationDisplayType;
import com.myservers.backend.notifications.enums.NotificationTargetType;
import com.myservers.backend.notifications.enums.NotificationType;
import com.myservers.backend.security.auth.entities.Admin;
import com.myservers.backend.security.auth.entities.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String title;
    
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationDisplayType displayType;
    
    @Column(nullable = false)
    private Date createdAt;
    
    @Column(nullable = false)
    private Date expiresAt;
    
    @Column
    private String link;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    @JsonIgnore
    private Admin createdBy;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationTargetType targetType;
    
    // For specific users
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "notification_users",
        joinColumns = @JoinColumn(name = "notification_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @JsonIgnore
    private Set<User> targetUsers = new HashSet<>();
    
    // For specific user groups
    @ElementCollection
    @CollectionTable(name = "notification_user_groups", joinColumns = @JoinColumn(name = "notification_id"))
    @Column(name = "user_group_id")
    private Set<Long> targetUserGroups = new HashSet<>();
    
    // Notification recipients (for tracking who received the notification)
    @OneToMany(mappedBy = "notification", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<NotificationRecipient> recipients = new HashSet<>();
    
    @PrePersist
    protected void onCreate() {
        createdAt = new Date();
        if (expiresAt == null) {
            // Default expiration: 30 days from creation
            expiresAt = new Date(System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000));
        }
    }
} 