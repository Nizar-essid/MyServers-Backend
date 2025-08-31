package com.myservers.backend.users.repositories;

import com.myservers.backend.users.entities.BalanceChangeHistory;
import com.myservers.backend.security.auth.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface BalanceChangeHistoryRepository extends JpaRepository<BalanceChangeHistory, Long> {

    List<BalanceChangeHistory> findByUserOrderByChangeDateDesc(User user);

    List<BalanceChangeHistory> findByUserAndIsActiveOrderByChangeDateDesc(User user, Boolean isActive);

    @Query("SELECT bch FROM BalanceChangeHistory bch WHERE bch.isActive = true ORDER BY bch.changeDate DESC")
    Page<BalanceChangeHistory> findAllActiveOrderByChangeDateDesc(Pageable pageable);

    @Query("SELECT bch FROM BalanceChangeHistory bch ORDER BY bch.changeDate DESC")
    Page<BalanceChangeHistory> findAllOrderByChangeDateDesc(Pageable pageable);

    @Query("SELECT bch FROM BalanceChangeHistory bch WHERE bch.user.id = :userId AND bch.isActive = true ORDER BY bch.changeDate DESC")
    List<BalanceChangeHistory> findByUserIdOrderByChangeDateDesc(@Param("userId") Integer userId);

    @Query("SELECT bch FROM BalanceChangeHistory bch WHERE bch.admin.id = :adminId AND bch.isActive = true ORDER BY bch.changeDate DESC")
    List<BalanceChangeHistory> findByAdminIdOrderByChangeDateDesc(@Param("adminId") Integer adminId);

    @Query("SELECT bch FROM BalanceChangeHistory bch WHERE bch.paymentStatus = :paymentStatus AND bch.isActive = true ORDER BY bch.changeDate DESC")
    List<BalanceChangeHistory> findByPaymentStatusOrderByChangeDateDesc(@Param("paymentStatus") BalanceChangeHistory.PaymentStatus paymentStatus);

    @Query("SELECT bch FROM BalanceChangeHistory bch WHERE bch.changeType = :changeType AND bch.isActive = true ORDER BY bch.changeDate DESC")
    List<BalanceChangeHistory> findByChangeTypeOrderByChangeDateDesc(@Param("changeType") BalanceChangeHistory.ChangeType changeType);

    @Query("SELECT bch FROM BalanceChangeHistory bch WHERE bch.changeDate BETWEEN :startDate AND :endDate AND bch.isActive = true ORDER BY bch.changeDate DESC")
    List<BalanceChangeHistory> findByChangeDateBetweenOrderByChangeDateDesc(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

    @Query("SELECT bch FROM BalanceChangeHistory bch WHERE bch.user.email LIKE %:searchTerm% OR bch.user.firstname LIKE %:searchTerm% OR bch.user.lastname LIKE %:searchTerm% AND bch.isActive = true ORDER BY bch.changeDate DESC")
    List<BalanceChangeHistory> findByUserEmailOrNameContainingOrderByChangeDateDesc(@Param("searchTerm") String searchTerm);

    @Query("SELECT COUNT(bch) FROM BalanceChangeHistory bch WHERE bch.paymentStatus = 'UNPAID' AND bch.isActive = true")
    Long countUnpaidChanges();

    @Query("SELECT COUNT(bch) FROM BalanceChangeHistory bch WHERE bch.paymentStatus = 'PAID' AND bch.isActive = true")
    Long countPaidChanges();

    @Query("SELECT SUM(bch.amount) FROM BalanceChangeHistory bch WHERE bch.paymentStatus = 'UNPAID' AND bch.isActive = true")
    Double sumUnpaidAmounts();

    @Query("SELECT SUM(bch.amount) FROM BalanceChangeHistory bch WHERE bch.paymentStatus = 'PAID' AND bch.isActive = true")
    Double sumPaidAmounts();

    @Query("SELECT SUM(bch.amount) FROM BalanceChangeHistory bch WHERE bch.changeDate BETWEEN :startDate AND :endDate AND bch.isActive = true")
    Double sumBalanceChangesByDateRange(@Param("startDate") Date startDate, @Param("endDate") Date endDate);
}
