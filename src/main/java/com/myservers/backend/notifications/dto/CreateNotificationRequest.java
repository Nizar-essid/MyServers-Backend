package com.myservers.backend.notifications.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.myservers.backend.notifications.enums.NotificationDisplayType;
import com.myservers.backend.notifications.enums.NotificationTargetType;
import com.myservers.backend.notifications.enums.NotificationType;
import lombok.*;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateNotificationRequest {
    
    @JsonProperty("title")
    private String title;
    
    @JsonProperty("content")
    private String content;
    
    @JsonProperty("type")
    private NotificationType type;
    
    @JsonProperty("displayType")
    private NotificationDisplayType displayType;
    
    @JsonProperty("targetType")
    private NotificationTargetType targetType;
    
    @JsonProperty("targetUserIds")
    private List<Integer> targetUserIds;
    
    @JsonProperty("targetUserGroupIds")
    private List<Integer> targetUserGroupIds;
    
    @JsonProperty("link")
    private String link;
    
    @JsonProperty("expiresAt")
    private Date expiresAt;
} 