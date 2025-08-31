package com.myservers.backend.notifications.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.myservers.backend.security.auth.entities.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@Table(name = "notification_recipients")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRecipient {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notification_id", nullable = false)
    @JsonIgnore
    private Notification notification;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;
    
    @Column(nullable = false)
    private boolean isRead = false;
    
    @Column
    private Date readAt;
    
    @Column(nullable = false)
    private Date receivedAt;
    
    @Column
    private Date clickedAt;
    
    @Column
    private String clickedLink;
    
    @PrePersist
    protected void onCreate() {
        receivedAt = new Date();
    }
} 