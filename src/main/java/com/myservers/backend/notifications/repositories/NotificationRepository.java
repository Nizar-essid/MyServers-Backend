package com.myservers.backend.notifications.repositories;

import com.myservers.backend.notifications.entities.Notification;
import com.myservers.backend.notifications.enums.NotificationDisplayType;
import com.myservers.backend.notifications.enums.NotificationTargetType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    @Query("SELECT n FROM Notification n WHERE n.expiresAt > :currentDate ORDER BY n.createdAt DESC")
    List<Notification> findActiveNotifications(@Param("currentDate") Date currentDate);
    
    @Query("SELECT n FROM Notification n WHERE n.targetType = 'ALL_USERS' AND n.expiresAt > :currentDate ORDER BY n.createdAt DESC")
    List<Notification> findActiveNotificationsForAllUsers(@Param("currentDate") Date currentDate);
    
    @Query("SELECT n FROM Notification n JOIN n.targetUsers u WHERE u.id = :userId AND n.expiresAt > :currentDate ORDER BY n.createdAt DESC")
    List<Notification> findActiveNotificationsForUser(@Param("userId") Long userId, @Param("currentDate") Date currentDate);
    
    @Query("SELECT n FROM Notification n JOIN n.targetUserGroups ug WHERE ug = :userGroupId AND n.expiresAt > :currentDate ORDER BY n.createdAt DESC")
    List<Notification> findActiveNotificationsForUserGroup(@Param("userGroupId") Long userGroupId, @Param("currentDate") Date currentDate);
    
    @Query("SELECT n FROM Notification n WHERE n.displayType = :displayType AND n.expiresAt > :currentDate ORDER BY n.createdAt DESC")
    List<Notification> findActiveNotificationsByDisplayType(@Param("displayType") NotificationDisplayType displayType, @Param("currentDate") Date currentDate);
    
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.targetType = 'ALL_USERS' AND n.expiresAt > :currentDate")
    long countActiveNotificationsForAllUsers(@Param("currentDate") Date currentDate);
    
    @Query("SELECT COUNT(n) FROM Notification n JOIN n.targetUsers u WHERE u.id = :userId AND n.expiresAt > :currentDate")
    long countActiveNotificationsForUser(@Param("userId") Long userId, @Param("currentDate") Date currentDate);
    
    @Query("SELECT COUNT(n) FROM Notification n JOIN n.targetUserGroups ug WHERE ug = :userGroupId AND n.expiresAt > :currentDate")
    long countActiveNotificationsForUserGroup(@Param("userGroupId") Long userGroupId, @Param("currentDate") Date currentDate);
} 