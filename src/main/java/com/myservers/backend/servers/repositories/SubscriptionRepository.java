package com.myservers.backend.servers.repositories;

import com.myservers.backend.servers.entities.Subscription;
import com.myservers.backend.servers.entities.SubscrptionState;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends CrudRepository<Subscription, Long> {
    Optional<Subscription> findByVerificationCodeAndPurchaser_IdAndState(String verification_code, Integer id, SubscrptionState state);

    List<Subscription> findByPurchaser_IdAndState(Integer id, SubscrptionState state);

    List<Subscription> findByState(SubscrptionState state);

    long countByStateAndDateLatestUpdateBefore(SubscrptionState state, Date date_latest_update);

    List<Subscription> findByStateAndDateLatestUpdateLessThanEqual(SubscrptionState state, Date date_latest_update);


    //  long countSubscriptionsCreatedBeforeOrIn(Date endDate);

    // Compter les abonnements par serveur
    @Query("SELECT COUNT(s) FROM Subscription s WHERE s.relatedCode.originServer.id = :serverId AND s.state = :state")
    long countByRelatedCode_OriginServer_IdAndState(@Param("serverId") Long serverId, @Param("state") SubscrptionState state);

    // Compter tous les abonnements par serveur
    @Query("SELECT COUNT(s) FROM Subscription s WHERE s.relatedCode.originServer.id = :serverId")
    long countByRelatedCode_OriginServer_Id(@Param("serverId") Long serverId);

    // Trouver les abonnements par serveur et état
    @Query("SELECT s FROM Subscription s WHERE s.relatedCode.originServer.id = :serverId AND s.state = :state")
    List<Subscription> findByRelatedCode_OriginServer_IdAndState(@Param("serverId") Long serverId, @Param("state") SubscrptionState state);
}
