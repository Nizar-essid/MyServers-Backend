package com.myservers.backend.users.services;

import com.myservers.backend.security.auth.entities.User;
import com.myservers.backend.users.entities.UserGroup;
import com.myservers.backend.users.entities.UserGroupAssignment;
import com.myservers.backend.users.repositories.UserGroupRepository;
import com.myservers.backend.users.repositories.UserGroupAssignmentRepository;
import com.myservers.backend.security.auth.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class UserGroupAssignmentService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserGroupRepository userGroupRepository;

    @Autowired
    private UserGroupAssignmentRepository userGroupAssignmentRepository;

        /**
     * Assign a user to a specific group
     */
    public boolean assignUserToGroup(Integer userId, Long groupId) {
        Optional<User> userOpt = userRepository.findById(userId);
        Optional<UserGroup> groupOpt = userGroupRepository.findById(groupId);

        if (userOpt.isPresent() && groupOpt.isPresent()) {
            User user = userOpt.get();
            UserGroup group = groupOpt.get();

            // Check if assignment already exists
            if (!userGroupAssignmentRepository.existsByUserAndUserGroupAndIsActiveTrue(user, group)) {
                UserGroupAssignment assignment = UserGroupAssignment.builder()
                    .user(user)
                    .userGroup(group)
                    .assignedDate(new Date())
                    .isActive(true)
                    .build();

                userGroupAssignmentRepository.save(assignment);
                return true;
            }
        }
        return false;
    }

    /**
     * Remove a user from a specific group
     */
    public boolean removeUserFromGroup(Integer userId, Long groupId) {
        Optional<User> userOpt = userRepository.findById(userId);
        Optional<UserGroup> groupOpt = userGroupRepository.findById(groupId);

        if (userOpt.isPresent() && groupOpt.isPresent()) {
            User user = userOpt.get();
            UserGroup group = groupOpt.get();

            Optional<UserGroupAssignment> assignment = userGroupAssignmentRepository
                .findByUserAndUserGroupAndIsActiveTrue(user, group);

            if (assignment.isPresent()) {
                assignment.get().setIsActive(false);
                userGroupAssignmentRepository.save(assignment.get());
                return true;
            }
        }
        return false;
    }

    /**
     * Remove a user from all groups
     */
    public boolean removeUserFromAllGroups(Integer userId) {
        Optional<User> userOpt = userRepository.findById(userId);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            List<UserGroupAssignment> assignments = userGroupAssignmentRepository
                .findByUserAndIsActiveTrue(user);

            for (UserGroupAssignment assignment : assignments) {
                assignment.setIsActive(false);
                userGroupAssignmentRepository.save(assignment);
            }
            return true;
        }
        return false;
    }

    /**
     * Get all users in a specific group
     */
    public List<User> getUsersInGroup(Long groupId) {
        Optional<UserGroup> groupOpt = userGroupRepository.findById(groupId);
        if (groupOpt.isPresent()) {
            return userGroupAssignmentRepository.findUsersInGroup(groupOpt.get());
        }
        return List.of();
    }

    /**
     * Get users not assigned to any group
     */
    public List<User> getUsersWithoutGroup() {
        return userGroupAssignmentRepository.findUsersWithoutGroups();
    }

    /**
     * Get all groups for a specific user
     */
    public List<UserGroup> getUserGroups(Integer userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            return userGroupAssignmentRepository.findGroupsForUser(userOpt.get());
        }
        return List.of();
    }

    /**
     * Get the number of users in a group
     */
    public long getUsersCountInGroup(Long groupId) {
        Optional<UserGroup> groupOpt = userGroupRepository.findById(groupId);
        if (groupOpt.isPresent()) {
            return userGroupAssignmentRepository.countByUserGroupAndIsActiveTrue(groupOpt.get());
        }
        return 0;
    }

    /**
     * Get the number of groups for a user
     */
    public long getGroupsCountForUser(Integer userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            return userGroupAssignmentRepository.countByUserAndIsActiveTrue(userOpt.get());
        }
        return 0;
    }
}
