package com.myservers.backend.notifications.services;

import com.myservers.backend.notifications.dto.CreateNotificationRequest;
import com.myservers.backend.notifications.dto.NotificationResponse;
import com.myservers.backend.notifications.entities.Notification;
import com.myservers.backend.notifications.entities.NotificationRecipient;
import com.myservers.backend.notifications.enums.NotificationDisplayType;
import com.myservers.backend.notifications.enums.NotificationTargetType;
import com.myservers.backend.notifications.enums.NotificationType;
import com.myservers.backend.notifications.repositories.NotificationRecipientRepository;
import com.myservers.backend.notifications.repositories.NotificationRepository;
import com.myservers.backend.security.auth.entities.Admin;
import com.myservers.backend.security.auth.entities.Role;
import com.myservers.backend.security.auth.entities.User;
import com.myservers.backend.security.auth.repositories.UserRepository;
import com.myservers.backend.security.auth.repositories.AdminRepository;
import com.myservers.backend.users.repositories.UserGroupRepository;
import com.myservers.backend.users.classes.GeneralResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private NotificationRecipientRepository recipientRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private UserGroupRepository userGroupRepository;

    @Transactional
    public GeneralResponse createNotification(CreateNotificationRequest request, Admin admin) {
        try {
            // Validate request
            if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
                return GeneralResponse.builder()
                        .status(400L)
                        .result("Notification title is required")
                        .trueFalse(false)
                        .build();
            }

            if (request.getContent() == null || request.getContent().trim().isEmpty()) {
                return GeneralResponse.builder()
                        .status(400L)
                        .result("Notification content is required")
                        .trueFalse(false)
                        .build();
            }

            // Create notification
            Notification notification = Notification.builder()
                    .title(request.getTitle())
                    .content(request.getContent())
                    .type(request.getType() != null ? request.getType() : NotificationType.MANUAL_NOTIFICATION)
                    .displayType(request.getDisplayType() != null ? request.getDisplayType() : NotificationDisplayType.DROPDOWN_NOTIFICATION)
                    .targetType(request.getTargetType())
                    .link(request.getLink())
                    .expiresAt(request.getExpiresAt())
                    .createdBy(admin)
                    .build();

            // Set target users or groups
            if (request.getTargetType() == NotificationTargetType.SPECIFIC_USERS && request.getTargetUserIds() != null) {
                Set<User> targetUsers = userRepository.findAllById(request.getTargetUserIds()).stream().collect(Collectors.toSet());
                notification.setTargetUsers(targetUsers);
            } else if (request.getTargetType() == NotificationTargetType.SPECIFIC_USER_GROUPS && request.getTargetUserGroupIds() != null) {
                notification.setTargetUserGroups(new HashSet<>(request.getTargetUserGroupIds().stream().map(Long::valueOf).collect(Collectors.toSet())));
            }

            notification = notificationRepository.save(notification);

            // Create recipients
            createRecipients(notification);

            return GeneralResponse.builder()
                    .status(200L)
                    .result("Notification created successfully")
                    .trueFalse(true)
                    .build();

        } catch (Exception e) {
            return GeneralResponse.builder()
                    .status(500L)
                    .result("Error creating notification: " + e.getMessage())
                    .trueFalse(false)
                    .build();
        }
    }

    private void createRecipients(Notification notification) {
        List<User> targetUsers = new ArrayList<>();

        if (notification.getTargetType() == NotificationTargetType.ALL_USERS) {
            // Only get active users
            targetUsers = userRepository.findByState(true);
            System.out.println("Found " + targetUsers.size() + " active users for notification recipients");
        } else if (notification.getTargetType() == NotificationTargetType.SPECIFIC_USERS) {
            targetUsers = new ArrayList<>(notification.getTargetUsers());
            System.out.println("Found " + targetUsers.size() + " specific users for notification recipients");
        } else if (notification.getTargetType() == NotificationTargetType.SPECIFIC_USER_GROUPS) {
            // Get users from specified groups
            for (Long groupId : notification.getTargetUserGroups()) {
                // This would need to be implemented based on your user group structure
                // For now, we'll assume there's a method to get users by group
                // targetUsers.addAll(userGroupRepository.findUsersByGroupId(groupId));
            }
            System.out.println("Found " + targetUsers.size() + " users from groups for notification recipients");
        }

        System.out.println("Creating " + targetUsers.size() + " notification recipients");

        // Create recipients for all target users
        for (User user : targetUsers) {
            NotificationRecipient recipient = NotificationRecipient.builder()
                    .notification(notification)
                    .user(user)
                    .build();
            recipientRepository.save(recipient);
            System.out.println("Created recipient for user ID: " + user.getId());
        }

        System.out.println("Finished creating notification recipients");
    }

    public GeneralResponse getUserNotifications(Integer userId) {
        try {
            List<NotificationRecipient> recipients = recipientRepository.findByUserId(userId);

            List<NotificationResponse> notifications = recipients.stream()
                    .map(this::mapToNotificationResponse)
                    .collect(Collectors.toList());

            return GeneralResponse.builder()
                    .status(200L)
                    .result("Notifications retrieved successfully")
                    .data(new ArrayList<>(notifications))
                    .trueFalse(true)
                    .build();

        } catch (Exception e) {
            return GeneralResponse.builder()
                    .status(500L)
                    .result("Error retrieving notifications: " + e.getMessage())
                    .trueFalse(false)
                    .build();
        }
    }

    public GeneralResponse getUserNotificationsByDisplayType(Integer userId, NotificationDisplayType displayType) {
        try {
            List<NotificationRecipient> recipients = recipientRepository.findByUserIdAndDisplayType(userId, displayType);
            System.out.println("Found " + recipients.size() + " notifications for user ID: " + userId + " with display type: " + displayType);

            List<NotificationResponse> notifications = recipients.stream()
                    .map(this::mapToNotificationResponse)
                    .collect(Collectors.toList());

            return GeneralResponse.builder()
                    .status(200L)
                    .result("Notifications retrieved successfully")
                    .data(new ArrayList<>(notifications))
                    .trueFalse(true)
                    .build();

        } catch (Exception e) {
            System.err.println("Error retrieving notifications: " + e.getMessage());
            return GeneralResponse.builder()
                    .status(500L)
                    .result("Error retrieving notifications: " + e.getMessage())
                    .trueFalse(false)
                    .build();
        }
    }

    public GeneralResponse getUnreadCount(Integer userId) {
        try {
            long count = recipientRepository.countUnreadByUserId(userId);

            return GeneralResponse.builder()
                    .status(200L)
                    .result("Unread count retrieved successfully")
                    .data(new ArrayList<>(Arrays.asList((int) count)))
                    .trueFalse(true)
                    .build();

        } catch (Exception e) {
            return GeneralResponse.builder()
                    .status(500L)
                    .result("Error retrieving unread count: " + e.getMessage())
                    .trueFalse(false)
                    .build();
        }
    }

    @Transactional
    public GeneralResponse markAsRead(Integer userId, Long notificationId) {
        try {
            recipientRepository.markAsRead(userId, notificationId, new Date());

            return GeneralResponse.builder()
                    .status(200L)
                    .result("Notification marked as read")
                    .trueFalse(true)
                    .build();

        } catch (Exception e) {
            return GeneralResponse.builder()
                    .status(500L)
                    .result("Error marking notification as read: " + e.getMessage())
                    .trueFalse(false)
                    .build();
        }
    }

    @Transactional
    public GeneralResponse markAllAsRead(Integer userId) {
        try {
            recipientRepository.markAllAsRead(userId, new Date());

            return GeneralResponse.builder()
                    .status(200L)
                    .result("All notifications marked as read")
                    .trueFalse(true)
                    .build();

        } catch (Exception e) {
            return GeneralResponse.builder()
                    .status(500L)
                    .result("Error marking notifications as read: " + e.getMessage())
                    .trueFalse(false)
                    .build();
        }
    }

    @Transactional
    public GeneralResponse recordClick(Integer userId, Long notificationId, String clickedLink) {
        try {
            recipientRepository.recordClick(userId, notificationId, new Date(), clickedLink);

            return GeneralResponse.builder()
                    .status(200L)
                    .result("Click recorded successfully")
                    .trueFalse(true)
                    .build();

        } catch (Exception e) {
            return GeneralResponse.builder()
                    .status(500L)
                    .result("Error recording click: " + e.getMessage())
                    .trueFalse(false)
                    .build();
        }
    }

    private NotificationResponse mapToNotificationResponse(NotificationRecipient recipient) {
        Notification notification = recipient.getNotification();

        return NotificationResponse.builder()
                .id(notification.getId())
                .title(notification.getTitle())
                .content(notification.getContent())
                .type(notification.getType())
                .displayType(notification.getDisplayType())
                .createdAt(notification.getCreatedAt())
                .expiresAt(notification.getExpiresAt())
                .link(notification.getLink())
                .isRead(recipient.isRead())
                .readAt(recipient.getReadAt())
                .clickedAt(recipient.getClickedAt())
                .build();
    }

    // Method to create automatic notifications for admin actions
    @Transactional
    public void createAutomaticNotification(String title, String content, NotificationType type,
                                          String link, NotificationDisplayType displayType,
                                          NotificationTargetType targetType,
                                          List<Integer> targetUserIds, List<Integer> targetUserGroupIds) {
        try {
            CreateNotificationRequest request = CreateNotificationRequest.builder()
                    .title(title)
                    .content(content)
                    .type(type)
                    .displayType(displayType)
                    .targetType(targetType)
                    .targetUserIds(targetUserIds)
                    .targetUserGroupIds(targetUserGroupIds)
                    .link(link)
                    .build();

            // Get an Admin entity to record as the notification creator
            Admin admin = adminRepository.findAll().stream().findFirst().orElse(null);

            if (admin != null) {
                createNotification(request, admin);
            } else {
                System.err.println("NotificationService: No admin found to assign as notification creator. Notification not created.");
            }
        } catch (Exception e) {
            // Log error but don't throw exception to avoid breaking main functionality
            System.err.println("Error creating automatic notification: " + e.getMessage());
        }
    }
}
