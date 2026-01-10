package com.myservers.backend.shop.controllers;

import com.myservers.backend.servers.classes.GeneralResponse;
import com.myservers.backend.shop.services.ShopAccessService;
import com.myservers.backend.users.dto.UserGroupDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@CrossOrigin(origins = "*", exposedHeaders = "**")
@RestController
@RequestMapping("/api/v1/shop/admin/access")
@RequiredArgsConstructor
public class ShopAccessController {

    private final ShopAccessService shopAccessService;

    @GetMapping("/groups")
    public GeneralResponse getGroupsWithAccess() {
        try {
            java.util.List<UserGroupDto> groups = shopAccessService.getGroupsWithAccess();
            return GeneralResponse.builder()
                    .status(200L)
                    .result("Success")
                    .data(new ArrayList<>(groups))
                    .build();
        } catch (Exception e) {
            return GeneralResponse.builder()
                    .status(500L)
                    .result("Error: " + e.getMessage())
                    .build();
        }
    }

    @PostMapping("/groups/{groupId}/grant")
    public GeneralResponse grantAccess(@PathVariable Long groupId) {
        try {
            shopAccessService.grantAccess(groupId);
            return GeneralResponse.builder()
                    .status(200L)
                    .result("Access granted successfully")
                    .build();
        } catch (Exception e) {
            return GeneralResponse.builder()
                    .status(500L)
                    .result("Error: " + e.getMessage())
                    .build();
        }
    }

    @PostMapping("/groups/{groupId}/revoke")
    public GeneralResponse revokeAccess(@PathVariable Long groupId) {
        try {
            shopAccessService.revokeAccess(groupId);
            return GeneralResponse.builder()
                    .status(200L)
                    .result("Access revoked successfully")
                    .build();
        } catch (Exception e) {
            return GeneralResponse.builder()
                    .status(500L)
                    .result("Error: " + e.getMessage())
                    .build();
        }
    }
}

