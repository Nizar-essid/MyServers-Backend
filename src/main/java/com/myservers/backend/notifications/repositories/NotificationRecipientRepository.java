package com.myservers.backend.notifications.repositories;

import com.myservers.backend.notifications.entities.NotificationRecipient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRecipientRepository extends JpaRepository<NotificationRecipient, Long> {

    @Query("SELECT nr FROM NotificationRecipient nr WHERE nr.user.id = :userId AND nr.notification.id = :notificationId")
    Optional<NotificationRecipient> findByUserIdAndNotificationId(@Param("userId") Integer userId, @Param("notificationId") Long notificationId);

    @Query("SELECT nr FROM NotificationRecipient nr WHERE nr.user.id = :userId ORDER BY nr.receivedAt DESC")
    List<NotificationRecipient> findByUserId(@Param("userId") Integer userId);

    @Query("SELECT nr FROM NotificationRecipient nr WHERE nr.user.id = :userId AND nr.isRead = false ORDER BY nr.receivedAt DESC")
    List<NotificationRecipient> findUnreadByUserId(@Param("userId") Integer userId);

    @Query("SELECT COUNT(nr) FROM NotificationRecipient nr WHERE nr.user.id = :userId AND nr.isRead = false")
    long countUnreadByUserId(@Param("userId") Integer userId);

    @Query("SELECT nr FROM NotificationRecipient nr WHERE nr.user.id = :userId AND nr.notification.displayType = :displayType AND nr.notification.expiresAt > CURRENT_TIMESTAMP ORDER BY nr.receivedAt DESC")
    List<NotificationRecipient> findByUserIdAndDisplayType(@Param("userId") Integer userId, @Param("displayType") com.myservers.backend.notifications.enums.NotificationDisplayType displayType);

    @Modifying
    @Query("UPDATE NotificationRecipient nr SET nr.isRead = true, nr.readAt = :readAt WHERE nr.user.id = :userId AND nr.notification.id = :notificationId")
    void markAsRead(@Param("userId") Integer userId, @Param("notificationId") Long notificationId, @Param("readAt") Date readAt);

    @Modifying
    @Query("UPDATE NotificationRecipient nr SET nr.isRead = true, nr.readAt = :readAt WHERE nr.user.id = :userId")
    void markAllAsRead(@Param("userId") Integer userId, @Param("readAt") Date readAt);

    @Modifying
    @Query("UPDATE NotificationRecipient nr SET nr.clickedAt = :clickedAt, nr.clickedLink = :clickedLink WHERE nr.user.id = :userId AND nr.notification.id = :notificationId")
    void recordClick(@Param("userId") Integer userId, @Param("notificationId") Long notificationId, @Param("clickedAt") Date clickedAt, @Param("clickedLink") String clickedLink);
}
