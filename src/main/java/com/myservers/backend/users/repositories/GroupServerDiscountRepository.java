package com.myservers.backend.users.repositories;

import com.myservers.backend.servers.entities.Server;
import com.myservers.backend.users.entities.GroupServerDiscount;
import com.myservers.backend.users.entities.UserGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupServerDiscountRepository extends JpaRepository<GroupServerDiscount, Long> {

    List<GroupServerDiscount> findByUserGroupAndIsActiveTrue(UserGroup userGroup);

    List<GroupServerDiscount> findByServerAndIsActiveTrue(Server server);

    Optional<GroupServerDiscount> findByUserGroupAndServerAndIsActiveTrue(UserGroup userGroup, Server server);

    @Query("SELECT gsd FROM GroupServerDiscount gsd WHERE gsd.userGroup = :userGroup AND gsd.server = :server AND gsd.isActive = true")
    List<GroupServerDiscount> findAllByUserGroupAndServer(@Param("userGroup") UserGroup userGroup, @Param("server") Server server);
}
