package com.myservers.backend.users.repositories;

import com.myservers.backend.servers.entities.Server;
import com.myservers.backend.users.entities.UserPrice;
import com.myservers.backend.security.auth.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserPriceRepository extends JpaRepository<UserPrice, Long> {

    List<UserPrice> findByUserAndIsActiveTrue(User user);

    List<UserPrice> findByServerAndIsActiveTrue(Server server);

    Optional<UserPrice> findByUserAndServerAndDurationDaysAndIsActiveTrue(
        User user, Server server, Integer durationDays);

    @Query("SELECT up FROM UserPrice up WHERE up.user = :user AND up.server = :server AND up.isActive = true")
    List<UserPrice> findByUserAndServer(@Param("user") User user, @Param("server") Server server);

    @Query("SELECT up FROM UserPrice up WHERE up.user.state = true AND up.server.state = true AND up.isActive = true")
    List<UserPrice> findAllActiveUserPrices();
}
