package com.myservers.backend.users.repositories;

import com.myservers.backend.security.auth.entities.User;
import com.myservers.backend.servers.entities.Server;
import com.myservers.backend.users.entities.UserServerDiscount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserServerDiscountRepository extends JpaRepository<UserServerDiscount, Long> {

    List<UserServerDiscount> findByUserAndIsActiveTrue(User user);

    List<UserServerDiscount> findByServerAndIsActiveTrue(Server server);

    Optional<UserServerDiscount> findByUserAndServerAndIsActiveTrue(User user, Server server);

    @Query("SELECT usd FROM UserServerDiscount usd WHERE usd.user = :user AND usd.server = :server AND usd.isActive = true")
    List<UserServerDiscount> findAllByUserAndServer(@Param("user") User user, @Param("server") Server server);
}
