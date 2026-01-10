package com.myservers.backend.shop.repositories;

import com.myservers.backend.shop.entities.ShopAccess;
import com.myservers.backend.users.entities.UserGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShopAccessRepository extends JpaRepository<ShopAccess, Long> {
    Optional<ShopAccess> findByUserGroup(UserGroup userGroup);
    List<ShopAccess> findByHasAccessTrue();
}

