package com.myservers.backend.users.repositories;

import com.myservers.backend.security.auth.entities.User;
import com.myservers.backend.users.entities.UserGroup;
import com.myservers.backend.users.entities.UserGroupAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserGroupAssignmentRepository extends JpaRepository<UserGroupAssignment, Long> {

    // Find all active assignments for a user
    List<UserGroupAssignment> findByUserAndIsActiveTrue(User user);

    // Find all active assignments for a group
    List<UserGroupAssignment> findByUserGroupAndIsActiveTrue(UserGroup userGroup);

    // Find specific assignment
    Optional<UserGroupAssignment> findByUserAndUserGroupAndIsActiveTrue(User user, UserGroup userGroup);

    // Check if user is assigned to group
    boolean existsByUserAndUserGroupAndIsActiveTrue(User user, UserGroup userGroup);

    // Count active assignments for a group
    long countByUserGroupAndIsActiveTrue(UserGroup userGroup);

    // Count active assignments for a user
    long countByUserAndIsActiveTrue(User user);

    // Get all users in a group (using assignment table)
    @Query("SELECT uga.user FROM UserGroupAssignment uga WHERE uga.userGroup = :userGroup AND uga.isActive = true")
    List<User> findUsersInGroup(@Param("userGroup") UserGroup userGroup);

    // Get all groups for a user (using assignment table)
    @Query("SELECT uga.userGroup FROM UserGroupAssignment uga WHERE uga.user = :user AND uga.isActive = true")
    List<UserGroup> findGroupsForUser(@Param("user") User user);

    // Get users not assigned to any group
    @Query("SELECT u FROM User u WHERE u.state = true AND u NOT IN " +
           "(SELECT DISTINCT uga.user FROM UserGroupAssignment uga WHERE uga.isActive = true)")
    List<User> findUsersWithoutGroups();
}
