package com.myservers.backend.users.repositories;

import com.myservers.backend.users.entities.UserGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserGroupRepository extends JpaRepository<UserGroup, Long> {

    Optional<UserGroup> findByName(String name);

    List<UserGroup> findByIsActiveTrue();

    @Query("SELECT ug FROM UserGroup ug WHERE ug.isActive = true ORDER BY ug.name")
    List<UserGroup> findAllActiveGroups();

    boolean existsByName(String name);
}
