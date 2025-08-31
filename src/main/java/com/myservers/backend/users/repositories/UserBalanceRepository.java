package com.myservers.backend.users.repositories;

import com.myservers.backend.users.entities.UserBalance;
import com.myservers.backend.security.auth.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserBalanceRepository extends JpaRepository<UserBalance, Long> {

    Optional<UserBalance> findByUser(User user);

    Optional<UserBalance> findByUserId(Long userId);

    @Query("SELECT ub FROM UserBalance ub WHERE ub.user.state = true AND ub.isActive = true")
    List<UserBalance> findAllActiveBalances();

    @Query("SELECT ub FROM UserBalance ub WHERE ub.balance > 0 AND ub.user.state = true AND ub.isActive = true")
    List<UserBalance> findUsersWithPositiveBalance();
}
