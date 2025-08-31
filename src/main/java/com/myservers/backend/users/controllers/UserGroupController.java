package com.myservers.backend.users.controllers;

import com.myservers.backend.users.entities.UserGroup;
import com.myservers.backend.users.services.UserGroupService;
import com.myservers.backend.security.auth.entities.Admin;
import com.myservers.backend.security.auth.services.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/users/groups")
@CrossOrigin(origins = "*")
public class UserGroupController {

    @Autowired
    private UserGroupService userGroupService;

    @Autowired
    private AdminService adminService;

    @GetMapping("/all")
    public ResponseEntity<?> getAllGroups() {
        try {
            List<UserGroup> groups = userGroupService.getAllGroups();
            return ResponseEntity.ok().body(Map.of(
                "status", 200,
                "message", "Groups retrieved successfully",
                "data", groups
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", 400,
                "message", "Error retrieving groups: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/active")
    public ResponseEntity<?> getActiveGroups() {
        try {
            List<UserGroup> groups = userGroupService.getAllActiveGroups();
            return ResponseEntity.ok().body(Map.of(
                "status", 200,
                "message", "Active groups retrieved successfully",
                "data", groups
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", 400,
                "message", "Error retrieving active groups: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getGroupById(@PathVariable Long id) {
        try {
            Optional<UserGroup> group = userGroupService.getGroupById(id);
            if (group.isPresent()) {
                return ResponseEntity.ok().body(Map.of(
                    "status", 200,
                    "message", "Group retrieved successfully",
                    "data", group.get()
                ));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", 400,
                "message", "Error retrieving group: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/create")
    public ResponseEntity<?> createGroup(@RequestBody UserGroup userGroup, Authentication authentication) {
        try {
            Admin admin = adminService.getAdminByEmail(authentication.getName());
            if (userGroupService.existsByName(userGroup.getName())) {
                return ResponseEntity.badRequest().body(Map.of(
                    "status", 400,
                    "message", "Group with this name already exists"
                ));
            }
            UserGroup createdGroup = userGroupService.createGroup(userGroup, admin);
            return ResponseEntity.ok().body(Map.of(
                "status", 200,
                "message", "Group created successfully",
                "data", createdGroup
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", 400,
                "message", "Error creating group: " + e.getMessage()
            ));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateGroup(@PathVariable Long id, @RequestBody UserGroup userGroup, Authentication authentication) {
        try {
            Admin admin = adminService.getAdminByEmail(authentication.getName());
            UserGroup updatedGroup = userGroupService.updateGroup(id, userGroup, admin);
            return ResponseEntity.ok().body(Map.of(
                "status", 200,
                "message", "Group updated successfully",
                "data", updatedGroup
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", 400,
                "message", "Error updating group: " + e.getMessage()
            ));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteGroup(@PathVariable Long id) {
        try {
            boolean deleted = userGroupService.deleteGroup(id);
            if (deleted) {
                return ResponseEntity.ok().body(Map.of(
                    "status", 200,
                    "message", "Group deleted successfully"
                ));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", 400,
                "message", "Error deleting group: " + e.getMessage()
            ));
        }
    }

    @PutMapping("/{id}/deactivate")
    public ResponseEntity<?> deactivateGroup(@PathVariable Long id) {
        try {
            boolean deactivated = userGroupService.deactivateGroup(id);
            if (deactivated) {
                return ResponseEntity.ok().body(Map.of(
                    "status", 200,
                    "message", "Group deactivated successfully"
                ));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", 400,
                "message", "Error deactivating group: " + e.getMessage()
            ));
        }
    }

    @PutMapping("/{id}/activate")
    public ResponseEntity<?> activateGroup(@PathVariable Long id) {
        try {
            boolean activated = userGroupService.activateGroup(id);
            if (activated) {
                return ResponseEntity.ok().body(Map.of(
                    "status", 200,
                    "message", "Group activated successfully"
                ));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", 400,
                "message", "Error activating group: " + e.getMessage()
            ));
        }
    }
}
