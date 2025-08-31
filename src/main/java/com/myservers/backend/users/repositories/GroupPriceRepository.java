package com.myservers.backend.users.repositories;

import com.myservers.backend.servers.entities.Server;
import com.myservers.backend.users.entities.GroupPrice;
import com.myservers.backend.users.entities.UserGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupPriceRepository extends JpaRepository<GroupPrice, Long> {

    List<GroupPrice> findByUserGroupAndIsActiveTrue(UserGroup userGroup);

    List<GroupPrice> findByServerAndIsActiveTrue(Server server);

    Optional<GroupPrice> findByUserGroupAndServerAndDurationDaysAndIsActiveTrue(
        UserGroup userGroup, Server server, Integer durationDays);

    @Query("SELECT gp FROM GroupPrice gp WHERE gp.userGroup = :userGroup AND gp.server = :server AND gp.isActive = true")
    List<GroupPrice> findByUserGroupAndServer(@Param("userGroup") UserGroup userGroup, @Param("server") Server server);

    @Query("SELECT gp FROM GroupPrice gp WHERE gp.userGroup.isActive = true AND gp.server.state = true AND gp.isActive = true")
    List<GroupPrice> findAllActivePrices();
}
