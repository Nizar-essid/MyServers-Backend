package com.myservers.backend.notifications.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.myservers.backend.notifications.enums.NotificationDisplayType;
import com.myservers.backend.notifications.enums.NotificationType;
import lombok.*;

import java.util.Date;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {
    
    @JsonProperty("id")
    private Long id;
    
    @JsonProperty("title")
    private String title;
    
    @JsonProperty("content")
    private String content;
    
    @JsonProperty("type")
    private NotificationType type;
    
    @JsonProperty("displayType")
    private NotificationDisplayType displayType;
    
    @JsonProperty("createdAt")
    private Date createdAt;
    
    @JsonProperty("expiresAt")
    private Date expiresAt;
    
    @JsonProperty("link")
    private String link;
    
    @JsonProperty("isRead")
    private boolean isRead;
    
    @JsonProperty("readAt")
    private Date readAt;
    
    @JsonProperty("clickedAt")
    private Date clickedAt;
} 