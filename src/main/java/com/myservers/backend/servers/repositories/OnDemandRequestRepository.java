package com.myservers.backend.servers.repositories;

import com.myservers.backend.servers.entities.OnDemandRequest;
import com.myservers.backend.servers.entities.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OnDemandRequestRepository extends JpaRepository<OnDemandRequest, Long> {

    // Trouver toutes les demandes d'un utilisateur
    List<OnDemandRequest> findByUser_IdOrderByRequestDateDesc(Long userId);

    // Trouver toutes les demandes d'un serveur
    List<OnDemandRequest> findByServer_IdOrderByRequestDateDesc(Long serverId);

    // Trouver les demandes par statut
    List<OnDemandRequest> findByStatusOrderByRequestDateDesc(RequestStatus status);

    // Trouver les demandes en attente
    List<OnDemandRequest> findByStatusOrderByRequestDateAsc(RequestStatus status);

    // Trouver les demandes d'un utilisateur par statut
    List<OnDemandRequest> findByUser_IdAndStatusOrderByRequestDateDesc(Long userId, RequestStatus status);

    // Vérifier si une MAC address est déjà utilisée pour un serveur
    @Query("SELECT COUNT(r) > 0 FROM OnDemandRequest r WHERE r.server.id = :serverId AND r.macAddress = :macAddress AND r.status IN ('APPROVED', 'PENDING')")
    boolean existsByServerIdAndMacAddress(@Param("serverId") Long serverId, @Param("macAddress") String macAddress);

    // Trouver les demandes approuvées pour un serveur
    List<OnDemandRequest> findByServer_IdAndStatusOrderByRequestDateDesc(Long serverId, RequestStatus status);
// Compter les demandes par statut
    long countByStatus(RequestStatus status);

    // Compter toutes les demandes
    long count();

    // Trouver les demandes approuvées par date
    List<OnDemandRequest> findByStatusAndRequestDateLessThanEqual(RequestStatus status, java.util.Date date);

    // Trouver les demandes approuvées dans une plage de dates
    List<OnDemandRequest> findByStatusAndRequestDateBetween(RequestStatus status, java.util.Date startDate, java.util.Date endDate);
}
