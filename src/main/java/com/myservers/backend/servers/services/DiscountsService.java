package com.myservers.backend.servers.services;

import com.myservers.backend.security.auth.entities.Admin;
import com.myservers.backend.security.auth.entities.User;
import com.myservers.backend.security.auth.repositories.UserRepository;
import com.myservers.backend.servers.entities.Server;
import com.myservers.backend.servers.repositories.ServerRepository;
import com.myservers.backend.users.entities.GroupServerDiscount;
import com.myservers.backend.users.entities.UserGroup;
import com.myservers.backend.users.entities.UserServerDiscount;
import com.myservers.backend.users.repositories.GroupServerDiscountRepository;
import com.myservers.backend.users.repositories.UserGroupAssignmentRepository;
import com.myservers.backend.users.repositories.UserGroupRepository;
import com.myservers.backend.users.repositories.UserServerDiscountRepository;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class DiscountsService {

    @Autowired private UserRepository userRepository;
    @Autowired private ServerRepository serverRepository;
    @Autowired private UserGroupRepository userGroupRepository;
    @Autowired private UserGroupAssignmentRepository userGroupAssignmentRepository;
    @Autowired private UserServerDiscountRepository userServerDiscountRepository;
    @Autowired private GroupServerDiscountRepository groupServerDiscountRepository;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ServerDiscountsDTO {
        private Long serverId;
        private List<UserDiscountDTO> userDiscounts;
        private List<GroupDiscountDTO> groupDiscounts;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserDiscountDTO {
        private Integer userId;
        private Double discountPercentage;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GroupDiscountDTO {
        private Long groupId;
        private Double discountPercentage;
    }

    public Optional<Server> getServer(Long serverId) {
        return serverRepository.findById(serverId);
    }

    public ServerDiscountsDTO getServerDiscounts(Long serverId) {
        Server server = serverRepository.findById(serverId)
                .orElseThrow(() -> new RuntimeException("Server not found"));

        List<UserDiscountDTO> userDiscounts = userServerDiscountRepository.findByServerAndIsActiveTrue(server)
                .stream()
                .map(usd -> UserDiscountDTO.builder()
                        .userId(usd.getUser().getId())
                        .discountPercentage(usd.getDiscountPercentage())
                        .build())
                .collect(Collectors.toList());

        List<GroupDiscountDTO> groupDiscounts = groupServerDiscountRepository.findByServerAndIsActiveTrue(server)
                .stream()
                .map(gsd -> GroupDiscountDTO.builder()
                        .groupId(gsd.getUserGroup().getId())
                        .discountPercentage(gsd.getDiscountPercentage())
                        .build())
                .collect(Collectors.toList());

        return ServerDiscountsDTO.builder()
                .serverId(serverId)
                .userDiscounts(userDiscounts)
                .groupDiscounts(groupDiscounts)
                .build();
    }

    public void setUserServerDiscount(Admin admin, Long serverId, Integer userId, Double discountPercentage) {
        if (discountPercentage == null || discountPercentage < 0 || discountPercentage > 100) {
            throw new IllegalArgumentException("Invalid discount percentage");
        }
        Server server = serverRepository.findById(serverId)
                .orElseThrow(() -> new RuntimeException("Server not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Deactivate existing if present
        userServerDiscountRepository.findByUserAndServerAndIsActiveTrue(user, server)
                .ifPresent(existing -> {
                    existing.setIsActive(false);
                    existing.setLatestUpdate(new Date());
                    existing.setUpdatedBy(admin);
                    userServerDiscountRepository.save(existing);
                });

        UserServerDiscount usd = UserServerDiscount.builder()
                .user(user)
                .server(server)
                .discountPercentage(discountPercentage)
                .isActive(true)
                .dateCreation(new Date())
                .createdBy(admin)
                .build();
        userServerDiscountRepository.save(usd);
    }

    public void removeUserServerDiscount(Admin admin, Long serverId, Integer userId) {
        Server server = serverRepository.findById(serverId)
                .orElseThrow(() -> new RuntimeException("Server not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        userServerDiscountRepository.findByUserAndServerAndIsActiveTrue(user, server)
                .ifPresent(existing -> {
                    existing.setIsActive(false);
                    existing.setLatestUpdate(new Date());
                    existing.setUpdatedBy(admin);
                    userServerDiscountRepository.save(existing);
                });
    }

    public void setGroupServerDiscount(Admin admin, Long serverId, Long groupId, Double discountPercentage) {
        if (discountPercentage == null || discountPercentage < 0 || discountPercentage > 100) {
            throw new IllegalArgumentException("Invalid discount percentage");
        }
        Server server = serverRepository.findById(serverId)
                .orElseThrow(() -> new RuntimeException("Server not found"));
        UserGroup group = userGroupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        // Deactivate existing if present
        groupServerDiscountRepository.findByUserGroupAndServerAndIsActiveTrue(group, server)
                .ifPresent(existing -> {
                    existing.setIsActive(false);
                    existing.setLatestUpdate(new Date());
                    existing.setUpdatedBy(admin);
                    groupServerDiscountRepository.save(existing);
                });

        GroupServerDiscount gsd = GroupServerDiscount.builder()
                .userGroup(group)
                .server(server)
                .discountPercentage(discountPercentage)
                .isActive(true)
                .dateCreation(new Date())
                .createdBy(admin)
                .build();
        groupServerDiscountRepository.save(gsd);
    }

    public void removeGroupServerDiscount(Admin admin, Long serverId, Long groupId) {
        Server server = serverRepository.findById(serverId)
                .orElseThrow(() -> new RuntimeException("Server not found"));
        UserGroup group = userGroupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        groupServerDiscountRepository.findByUserGroupAndServerAndIsActiveTrue(group, server)
                .ifPresent(existing -> {
                    existing.setIsActive(false);
                    existing.setLatestUpdate(new Date());
                    existing.setUpdatedBy(admin);
                    groupServerDiscountRepository.save(existing);
                });
    }

    // Compute effective per-server discount as MAX among user-level and any group-level server discounts
    public Optional<Double> computeEffectiveDiscount(Integer userId, Long serverId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Server server = serverRepository.findById(serverId)
                .orElseThrow(() -> new RuntimeException("Server not found"));

        Double userSpecific = userServerDiscountRepository.findByUserAndServerAndIsActiveTrue(user, server)
                .map(UserServerDiscount::getDiscountPercentage)
                .orElse(null);

        // Get groups for user
        var groups = userGroupAssignmentRepository.findGroupsForUser(user);
        List<Double> groupDiscounts = new ArrayList<>();
        if (groups != null) {
            for (UserGroup g : groups) {
                groupServerDiscountRepository.findByUserGroupAndServerAndIsActiveTrue(g, server)
                        .map(GroupServerDiscount::getDiscountPercentage)
                        .ifPresent(groupDiscounts::add);
            }
        }

        List<Double> values = new ArrayList<>();
        if (userSpecific != null) values.add(userSpecific);
        if (groupDiscounts != null) values.addAll(groupDiscounts);
        if (values.isEmpty()) return Optional.empty();
        return Optional.of(Collections.max(values));
    }

    // Compute default group discount for a user (MAX across all groups' general discountPercentage)
    public Optional<Double> computeDefaultGroupDiscount(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        var groups = userGroupAssignmentRepository.findGroupsForUser(user);
        if (groups == null || groups.isEmpty()) return Optional.empty();
        List<Double> dps = new ArrayList<>();
        for (UserGroup g : groups) {
            if (g.getDiscountPercentage() != null) dps.add(g.getDiscountPercentage());
        }
        if (dps.isEmpty()) return Optional.empty();
        return Optional.of(Collections.max(dps));
    }
}
