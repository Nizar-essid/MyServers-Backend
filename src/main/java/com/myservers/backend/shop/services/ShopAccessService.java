package com.myservers.backend.shop.services;

import com.myservers.backend.security.auth.entities.User;
import com.myservers.backend.shop.entities.ShopAccess;
import com.myservers.backend.shop.repositories.ShopAccessRepository;
import com.myservers.backend.users.dto.UserGroupDto;
import com.myservers.backend.users.entities.UserGroup;
import com.myservers.backend.users.repositories.UserGroupRepository;
import com.myservers.backend.users.services.DtoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ShopAccessService {

    @Autowired
    private ShopAccessRepository shopAccessRepository;

    @Autowired
    private UserGroupRepository userGroupRepository;

    @Autowired
    private DtoService dtoService;

    public boolean hasShopAccess(User user) {
        if (user == null || user.getUserGroups() == null || user.getUserGroups().isEmpty()) {
            return false;
        }

        List<UserGroup> userGroups = user.getUserGroups();
        for (UserGroup group : userGroups) {
            ShopAccess access = shopAccessRepository.findByUserGroup(group).orElse(null);
            if (access != null && access.getHasAccess()) {
                return true;
            }
        }
        return false;
    }

    public List<UserGroupDto> getGroupsWithAccess() {
        return shopAccessRepository.findByHasAccessTrue()
                .stream()
                .map(ShopAccess::getUserGroup)
                .map(dtoService::convertToUserGroupDto)
                .collect(Collectors.toList());
    }

    public void grantAccess(Long userGroupId) {
        UserGroup userGroup = userGroupRepository.findById(userGroupId)
                .orElseThrow(() -> new RuntimeException("User group not found"));

        ShopAccess access = shopAccessRepository.findByUserGroup(userGroup).orElse(null);
        if (access == null) {
            access = ShopAccess.builder()
                    .userGroup(userGroup)
                    .hasAccess(true)
                    .dateCreation(new Date())
                    .build();
        } else {
            access.setHasAccess(true);
            access.setLatestUpdate(new Date());
        }
        shopAccessRepository.save(access);
    }

    public void revokeAccess(Long userGroupId) {
        UserGroup userGroup = userGroupRepository.findById(userGroupId)
                .orElseThrow(() -> new RuntimeException("User group not found"));

        ShopAccess access = shopAccessRepository.findByUserGroup(userGroup).orElse(null);
        if (access != null) {
            access.setHasAccess(false);
            access.setLatestUpdate(new Date());
            shopAccessRepository.save(access);
        }
    }
}

