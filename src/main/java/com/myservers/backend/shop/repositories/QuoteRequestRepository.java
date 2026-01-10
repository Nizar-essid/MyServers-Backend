package com.myservers.backend.shop.repositories;

import com.myservers.backend.shop.entities.QuoteRequest;
import com.myservers.backend.shop.entities.QuoteRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuoteRequestRepository extends JpaRepository<QuoteRequest, Long> {
    List<QuoteRequest> findAllByOrderByDateCreationDesc();
    List<QuoteRequest> findByStatusOrderByDateCreationDesc(QuoteRequestStatus status);
    List<QuoteRequest> findByUserIdOrderByDateCreationDesc(Integer userId);
}

