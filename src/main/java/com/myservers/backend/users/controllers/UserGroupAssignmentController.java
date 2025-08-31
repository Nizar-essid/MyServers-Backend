package com.myservers.backend.users.controllers;

import com.myservers.backend.security.auth.entities.User;
import com.myservers.backend.users.entities.UserGroup;
import com.myservers.backend.users.services.UserGroupAssignmentService;
import com.myservers.backend.users.services.UserGroupService;
import com.myservers.backend.users.services.DtoService;
import com.myservers.backend.users.UserService.UserService;
import com.myservers.backend.users.dto.UserDto;
import com.myservers.backend.users.dto.UserGroupDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/user-group-assignments")
@CrossOrigin(origins = "*")
public class UserGroupAssignmentController {

    @Autowired
    private UserGroupAssignmentService userGroupAssignmentService;

    @Autowired
    private UserGroupService userGroupService;

    @Autowired
    private UserService userService;

    @Autowired
    private DtoService dtoService;

    /**
     * Assign a user to a group
     */
    @PostMapping("/assign")
    public ResponseEntity<?> assignUserToGroup(@RequestBody Map<String, Object> request) {
        try {
            Integer userId = (Integer) request.get("userId");
            Long groupId = Long.valueOf(request.get("groupId").toString());

            boolean success = userGroupAssignmentService.assignUserToGroup(userId, groupId);

            if (success) {
                return ResponseEntity.ok(Map.of(
                    "status", 200,
                    "result", "User assigned to group successfully"
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "status", 400,
                    "result", "Failed to assign user to group. User or group not found."
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", 400,
                "result", "Error: " + e.getMessage()
            ));
        }
    }

        /**
     * Remove a user from a specific group
     */
    @PostMapping("/remove")
    public ResponseEntity<?> removeUserFromGroup(@RequestBody Map<String, Object> request) {
        try {
            Integer userId = (Integer) request.get("userId");
            Long groupId = Long.valueOf(request.get("groupId").toString());

            boolean success = userGroupAssignmentService.removeUserFromGroup(userId, groupId);

            if (success) {
                return ResponseEntity.ok(Map.of(
                    "status", 200,
                    "result", "User removed from group successfully"
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "status", 400,
                    "result", "Failed to remove user from group. User or group not found."
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", 400,
                "result", "Error: " + e.getMessage()
            ));
        }
    }

    /**
     * Remove a user from all groups
     */
    @PostMapping("/remove-from-all")
    public ResponseEntity<?> removeUserFromAllGroups(@RequestBody Map<String, Object> request) {
        try {
            Integer userId = (Integer) request.get("userId");

            boolean success = userGroupAssignmentService.removeUserFromAllGroups(userId);

            if (success) {
                return ResponseEntity.ok(Map.of(
                    "status", 200,
                    "result", "User removed from all groups successfully"
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "status", 400,
                    "result", "Failed to remove user from groups. User not found."
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", 400,
                "result", "Error: " + e.getMessage()
            ));
        }
    }

    /**
     * Get all users in a specific group
     */
    @GetMapping("/group/{groupId}/users")
    public ResponseEntity<?> getUsersInGroup(@PathVariable Long groupId) {
        try {
            List<User> users = userGroupAssignmentService.getUsersInGroup(groupId);
            List<UserDto> userDtos = dtoService.convertToUserDtoList(users);

            return ResponseEntity.ok(Map.of(
                "status", 200,
                "result", "success",
                "data", userDtos
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", 400,
                "result", "Error: " + e.getMessage()
            ));
        }
    }

    /**
     * Get users not assigned to any group
     */
    @GetMapping("/users-without-group")
    public ResponseEntity<?> getUsersWithoutGroup() {
        try {
            List<User> users = userGroupAssignmentService.getUsersWithoutGroup();

            return ResponseEntity.ok(Map.of(
                "status", 200,
                "result", "success",
                "data", users
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", 400,
                "result", "Error: " + e.getMessage()
            ));
        }
    }

                /**
     * Get all groups for a specific user
     */
    @GetMapping("/user/{userId}/groups")
    public ResponseEntity<?> getUserGroups(@PathVariable Integer userId) {
        try {
            List<UserGroup> userGroups = userGroupAssignmentService.getUserGroups(userId);
            List<UserGroupDto> userGroupDtos = dtoService.convertToUserGroupDtoList(userGroups);

            return ResponseEntity.ok(Map.of(
                "status", 200,
                "result", "success",
                "data", userGroupDtos
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", 400,
                "result", "Error: " + e.getMessage()
            ));
        }
    }

    /**
     * Get group statistics
     */
    @GetMapping("/group/{groupId}/stats")
    public ResponseEntity<?> getGroupStats(@PathVariable Long groupId) {
        try {
            long userCount = userGroupAssignmentService.getUsersCountInGroup(groupId);
            var group = userGroupService.getGroupById(groupId);

            Map<String, Object> stats = new HashMap<>();
            stats.put("groupId", groupId);
            stats.put("userCount", userCount);
            stats.put("group", group.orElse(null));

            return ResponseEntity.ok(Map.of(
                "status", 200,
                "result", "success",
                "data", stats
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", 400,
                "result", "Error: " + e.getMessage()
            ));
        }
    }

    /**
     * Get all groups with user counts
     */
    @GetMapping("/groups-with-stats")
    public ResponseEntity<?> getGroupsWithStats() {
        try {
            List<UserGroup> groups = userGroupService.getAllGroups();

            Map<String, Object> result = new HashMap<>();
            for (UserGroup group : groups) {
                long userCount = userGroupAssignmentService.getUsersCountInGroup(group.getId());
                result.put(group.getName(), userCount);
            }

            return ResponseEntity.ok(Map.of(
                "status", 200,
                "result", "success",
                "data", result
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", 400,
                "result", "Error: " + e.getMessage()
            ));
        }
    }

        /**
     * Get all users for user management
     */
    @GetMapping("/all-users")
    public ResponseEntity<?> getAllUsers() {
      System.out.println("Fetching all users for user management");
        try {
            List<User> users = userService.getAllUsers();
            List<UserDto> userDtos = dtoService.convertToUserDtoList(users);

            return ResponseEntity.ok(Map.of(
                "status", 200,
                "result", "success",
                "data", userDtos
            ));
        } catch (Exception e) {
            System.err.println("Error fetching all users: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "status", 400,
                "result", "Error: " + e.getMessage()
            ));
        }
    }
}
