package com.myservers.backend.servers.controllers;

import com.myservers.backend.security.auth.entities.Admin;
import com.myservers.backend.security.config.JwtService;
import com.myservers.backend.servers.classes.GeneralResponse;
import com.myservers.backend.servers.services.DiscountsService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@CrossOrigin(origins = "*", exposedHeaders = "**")
@RestController
@RequestMapping("/api/v1/servers/admin")
public class ServerDiscountsAdminController {

    @Autowired private DiscountsService discountsService;
    @Autowired private JwtService authService;

    @GetMapping("/get_server_discounts")
    public GeneralResponse getServerDiscounts(@RequestParam("serverId") Long serverId) {
        try {
            var dto = discountsService.getServerDiscounts(serverId);
            return GeneralResponse.builder()
                    .status(200L)
                    .result("success")
                    .singleData(dto)
                    .trueFalse(true)
                    .build();
        } catch (Exception e) {
            return GeneralResponse.builder()
                    .status(500L)
                    .result("Error: " + e.getMessage())
                    .trueFalse(false)
                    .build();
        }
    }

    @PostMapping("/set_user_server_discount")
    public GeneralResponse setUserServerDiscount(@RequestBody Map<String, Object> body) {
        try {
            Long serverId = Long.valueOf(body.get("serverId").toString());
            Integer userId = Integer.valueOf(body.get("userId").toString());
            Double discountPercentage = Double.valueOf(body.get("discountPercentage").toString());
            Admin admin = authService.getAdmin();

            discountsService.setUserServerDiscount(admin, serverId, userId, discountPercentage);
            return GeneralResponse.builder()
                    .status(200L)
                    .result("success")
                    .trueFalse(true)
                    .build();
        } catch (Exception e) {
            return GeneralResponse.builder()
                    .status(500L)
                    .result("Error: " + e.getMessage())
                    .trueFalse(false)
                    .build();
        }
    }

    @PostMapping("/remove_user_server_discount")
    public GeneralResponse removeUserServerDiscount(@RequestBody Map<String, Object> body) {
        try {
            Long serverId = Long.valueOf(body.get("serverId").toString());
            Integer userId = Integer.valueOf(body.get("userId").toString());
            Admin admin = authService.getAdmin();

            discountsService.removeUserServerDiscount(admin, serverId, userId);
            return GeneralResponse.builder()
                    .status(200L)
                    .result("success")
                    .trueFalse(true)
                    .build();
        } catch (Exception e) {
            return GeneralResponse.builder()
                    .status(500L)
                    .result("Error: " + e.getMessage())
                    .trueFalse(false)
                    .build();
        }
    }

    @PostMapping("/set_group_server_discount")
    public GeneralResponse setGroupServerDiscount(@RequestBody Map<String, Object> body) {
        try {
            Long serverId = Long.valueOf(body.get("serverId").toString());
            Long groupId = Long.valueOf(body.get("groupId").toString());
            Double discountPercentage = Double.valueOf(body.get("discountPercentage").toString());
            Admin admin = authService.getAdmin();

            discountsService.setGroupServerDiscount(admin, serverId, groupId, discountPercentage);
            return GeneralResponse.builder()
                    .status(200L)
                    .result("success")
                    .trueFalse(true)
                    .build();
        } catch (Exception e) {
            return GeneralResponse.builder()
                    .status(500L)
                    .result("Error: " + e.getMessage())
                    .trueFalse(false)
                    .build();
        }
    }

    @PostMapping("/remove_group_server_discount")
    public GeneralResponse removeGroupServerDiscount(@RequestBody Map<String, Object> body) {
        try {
            Long serverId = Long.valueOf(body.get("serverId").toString());
            Long groupId = Long.valueOf(body.get("groupId").toString());
            Admin admin = authService.getAdmin();

            discountsService.removeGroupServerDiscount(admin, serverId, groupId);
            return GeneralResponse.builder()
                    .status(200L)
                    .result("success")
                    .trueFalse(true)
                    .build();
        } catch (Exception e) {
            return GeneralResponse.builder()
                    .status(500L)
                    .result("Error: " + e.getMessage())
                    .trueFalse(false)
                    .build();
        }
    }
}
