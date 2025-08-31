package com.myservers.backend.notifications.controllers;

import com.myservers.backend.notifications.dto.CreateNotificationRequest;
import com.myservers.backend.notifications.enums.NotificationDisplayType;
import com.myservers.backend.notifications.enums.NotificationTargetType;
import com.myservers.backend.notifications.services.NotificationService;
import com.myservers.backend.notifications.entities.Notification;
import com.myservers.backend.notifications.repositories.NotificationRepository;
import com.myservers.backend.security.auth.repositories.UserRepository;
import com.myservers.backend.security.auth.entities.User;
import com.myservers.backend.security.auth.entities.Admin;
import com.myservers.backend.security.auth.entities.Role;
import com.myservers.backend.security.config.JwtService;
import com.myservers.backend.users.classes.GeneralResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
@CrossOrigin(origins = "*", exposedHeaders = "**")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private JwtService authService;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    // Admin endpoints
    @PostMapping(value = "/admin/create", consumes = MediaType.APPLICATION_JSON_VALUE)
    public GeneralResponse createNotification(@RequestBody CreateNotificationRequest request) throws ParseException {
        try {
            User user = authService.getUser();
            if (user.getRole() != Role.ADMIN) {
                return GeneralResponse.builder()
                        .status(403L)
                        .result("Access denied. Admin privileges required.")
                        .trueFalse(false)
                        .build();
            }
            // Use NotificationService to create with a valid Admin creator
            return notificationService.createNotification(request, null);
        } catch (Exception e) {
            return GeneralResponse.builder()
                    .status(500L)
                    .result("Error creating notification: " + e.getMessage())
                    .trueFalse(false)
                    .build();
        }
    }

    // List all login notifications (optionally include expired)
    @GetMapping("/admin/login-notifications")
    public GeneralResponse listLoginNotifications(@RequestParam(name = "includeExpired", defaultValue = "true") boolean includeExpired) {
        try {
            User user = authService.getUser();
            if (user.getRole() != Role.ADMIN) {
                return GeneralResponse.builder()
                        .status(403L)
                        .result("Access denied. Admin privileges required.")
                        .trueFalse(false)
                        .build();
            }
            var now = new java.util.Date();
            java.util.List<Notification> list;
            if (includeExpired) {
                list = notificationRepository.findAll().stream()
                        .filter(n -> n.getDisplayType() == NotificationDisplayType.LOGIN_NOTIFICATION)
                        .sorted((a,b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                        .toList();
            } else {
                list = notificationRepository.findActiveNotificationsByDisplayType(NotificationDisplayType.LOGIN_NOTIFICATION, now);
            }
            return GeneralResponse.builder()
                    .status(200L)
                    .result("success")
                    .data(new java.util.ArrayList<>(list))
                    .trueFalse(true)
                    .build();
        } catch (Exception e) {
            return GeneralResponse.builder()
                    .status(500L)
                    .result("Error listing login notifications: " + e.getMessage())
                    .trueFalse(false)
                    .build();
        }
    }

    // Update a notification (title, content, link, displayType, target, expiresAt)
    @PutMapping(value = "/admin/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public GeneralResponse updateNotification(@PathVariable("id") Long id, @RequestBody Map<String, Object> body) {
        try {
            User user = authService.getUser();
            if (user.getRole() != Role.ADMIN) {
                return GeneralResponse.builder()
                        .status(403L)
                        .result("Access denied. Admin privileges required.")
                        .trueFalse(false)
                        .build();
            }
            Notification n = notificationRepository.findById(id).orElse(null);
            if (n == null) {
                return GeneralResponse.builder()
                        .status(404L)
                        .result("Notification not found")
                        .trueFalse(false)
                        .build();
            }
            if (body.containsKey("title")) n.setTitle(String.valueOf(body.get("title")));
            if (body.containsKey("content")) n.setContent(String.valueOf(body.get("content")));
            if (body.containsKey("link")) n.setLink(body.get("link") != null ? String.valueOf(body.get("link")) : null);
            if (body.containsKey("displayType")) n.setDisplayType(NotificationDisplayType.valueOf(String.valueOf(body.get("displayType"))));
            if (body.containsKey("targetType")) n.setTargetType(NotificationTargetType.valueOf(String.valueOf(body.get("targetType"))));
            if (body.containsKey("expiresAt") && body.get("expiresAt") != null) {
                Object v = body.get("expiresAt");
                if (v instanceof Number) {
                    n.setExpiresAt(new java.util.Date(((Number) v).longValue()));
                } else {
                    // Expect ISO string
                    n.setExpiresAt(java.util.Date.from(java.time.Instant.parse(String.valueOf(v))));
                }
            }
            // Update targets if provided
            if (body.containsKey("targetUserIds")) {
                java.util.List<Integer> ids = (java.util.List<Integer>) body.get("targetUserIds");
                if (ids != null) {
                    var users = new java.util.HashSet<>(userRepository.findAllById(ids));
                    n.setTargetUsers(users);
                }
            }
            if (body.containsKey("targetUserGroupIds")) {
                java.util.List<Number> gids = (java.util.List<Number>) body.get("targetUserGroupIds");
                java.util.Set<Long> set = new java.util.HashSet<>();
                if (gids != null) {
                    for (Number num : gids) set.add(num.longValue());
                }
                n.setTargetUserGroups(set);
            }

            notificationRepository.save(n);
            return GeneralResponse.builder()
                    .status(200L)
                    .result("Notification updated successfully")
                    .trueFalse(true)
                    .build();
        } catch (Exception e) {
            return GeneralResponse.builder()
                    .status(500L)
                    .result("Error updating notification: " + e.getMessage())
                    .trueFalse(false)
                    .build();
        }
    }

    // Delete a notification
    @DeleteMapping("/admin/{id}")
    public GeneralResponse deleteNotification(@PathVariable("id") Long id) {
        try {
            User user = authService.getUser();
            if (user.getRole() != Role.ADMIN) {
                return GeneralResponse.builder()
                        .status(403L)
                        .result("Access denied. Admin privileges required.")
                        .trueFalse(false)
                        .build();
            }
            if (!notificationRepository.existsById(id)) {
                return GeneralResponse.builder()
                        .status(404L)
                        .result("Notification not found")
                        .trueFalse(false)
                        .build();
            }
            notificationRepository.deleteById(id);
            return GeneralResponse.builder()
                    .status(200L)
                    .result("Notification deleted successfully")
                    .trueFalse(true)
                    .build();
        } catch (Exception e) {
            return GeneralResponse.builder()
                    .status(500L)
                    .result("Error deleting notification: " + e.getMessage())
                    .trueFalse(false)
                    .build();
        }
    }

    // Extend expiration by N days (negative to reduce)
    @PutMapping("/admin/{id}/extend")
    public GeneralResponse extendExpiration(@PathVariable("id") Long id, @RequestParam(name = "days") int days) {
        try {
            User user = authService.getUser();
            if (user.getRole() != Role.ADMIN) {
                return GeneralResponse.builder()
                        .status(403L)
                        .result("Access denied. Admin privileges required.")
                        .trueFalse(false)
                        .build();
            }
            Notification n = notificationRepository.findById(id).orElse(null);
            if (n == null) {
                return GeneralResponse.builder()
                        .status(404L)
                        .result("Notification not found")
                        .trueFalse(false)
                        .build();
            }
            long base = n.getExpiresAt() != null ? n.getExpiresAt().getTime() : System.currentTimeMillis();
            long updated = base + (long) days * 24L * 60L * 60L * 1000L;
            n.setExpiresAt(new java.util.Date(updated));
            notificationRepository.save(n);
            return GeneralResponse.builder()
                    .status(200L)
                    .result("Expiration updated successfully")
                    .trueFalse(true)
                    .build();
        } catch (Exception e) {
            return GeneralResponse.builder()
                    .status(500L)
                    .result("Error updating expiration: " + e.getMessage())
                    .trueFalse(false)
                    .build();
        }
    }

    // User endpoints
    @GetMapping("/user/all")
    public GeneralResponse getUserNotifications() throws ParseException {
        try {
            User user = authService.getUser();
            return notificationService.getUserNotifications(user.getId());
        } catch (Exception e) {
            return GeneralResponse.builder()
                    .status(500L)
                    .result("Error retrieving notifications: " + e.getMessage())
                    .trueFalse(false)
                    .build();
        }
    }

    @GetMapping("/user/dropdown")
    public GeneralResponse getDropdownNotifications() throws ParseException {
        try {
            User user = authService.getUser();
            return notificationService.getUserNotificationsByDisplayType(user.getId(), NotificationDisplayType.DROPDOWN_NOTIFICATION);
        } catch (Exception e) {
            return GeneralResponse.builder()
                    .status(500L)
                    .result("Error retrieving dropdown notifications: " + e.getMessage())
                    .trueFalse(false)
                    .build();
        }
    }

    @GetMapping("/user/login")
    public GeneralResponse getLoginNotifications() throws ParseException {
        try {
            User user = authService.getUser();
            return notificationService.getUserNotificationsByDisplayType(user.getId(), NotificationDisplayType.LOGIN_NOTIFICATION);
        } catch (Exception e) {
            return GeneralResponse.builder()
                    .status(500L)
                    .result("Error retrieving login notifications: " + e.getMessage())
                    .trueFalse(false)
                    .build();
        }
    }

    @GetMapping("/user/unread-count")
    public GeneralResponse getUnreadCount() throws ParseException {
        try {
            User user = authService.getUser();
            System.out.println("User ID: " + user.getId());
            return notificationService.getUnreadCount(user.getId());
        } catch (Exception e) {
            return GeneralResponse.builder()
                    .status(500L)
                    .result("Error retrieving unread count: " + e.getMessage())
                    .trueFalse(false)
                    .build();
        }
    }

    @PostMapping(value = "/user/mark-read", consumes = MediaType.APPLICATION_JSON_VALUE)
    public GeneralResponse markAsRead(@RequestBody Map<String, Object> requestBody) throws ParseException {
        try {
            User user = authService.getUser();
            Long notificationId = Long.valueOf(requestBody.get("notificationId").toString());

            return notificationService.markAsRead(user.getId(), notificationId);
        } catch (Exception e) {
            return GeneralResponse.builder()
                    .status(500L)
                    .result("Error marking notification as read: " + e.getMessage())
                    .trueFalse(false)
                    .build();
        }
    }

    @PostMapping(value = "/user/mark-all-read", consumes = MediaType.APPLICATION_JSON_VALUE)
    public GeneralResponse markAllAsRead() throws ParseException {
        try {
            User user = authService.getUser();
            return notificationService.markAllAsRead(user.getId());
        } catch (Exception e) {
            return GeneralResponse.builder()
                    .status(500L)
                    .result("Error marking all notifications as read: " + e.getMessage())
                    .trueFalse(false)
                    .build();
        }
    }

    @PostMapping(value = "/user/record-click", consumes = MediaType.APPLICATION_JSON_VALUE)
    public GeneralResponse recordClick(@RequestBody Map<String, Object> requestBody) throws ParseException {
        try {
            User user = authService.getUser();
            Long notificationId = Long.valueOf(requestBody.get("notificationId").toString());
            String clickedLink = requestBody.get("clickedLink") != null ? requestBody.get("clickedLink").toString() : null;

            return notificationService.recordClick(user.getId(), notificationId, clickedLink);
        } catch (Exception e) {
            return GeneralResponse.builder()
                    .status(500L)
                    .result("Error recording click: " + e.getMessage())
                    .trueFalse(false)
                    .build();
        }
    }
}
