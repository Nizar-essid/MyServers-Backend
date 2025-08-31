package com.myservers.backend.users;


import com.myservers.backend.security.auth.entities.User;
import com.myservers.backend.security.auth.services.AuthenticationService;
import com.myservers.backend.security.config.JwtService;
import com.myservers.backend.servers.classes.AllServersResponse;
import com.myservers.backend.servers.classes.CodeType;
import com.myservers.backend.servers.responsesDataType.AllServersRequestResponse;
import com.myservers.backend.users.classes.GeneralResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

import com.myservers.backend.users.entities.UserGroup;
import com.myservers.backend.users.services.UserGroupAssignmentService;

@CrossOrigin(origins = "*", exposedHeaders = "**")

@RestController
@RequestMapping("/api/v1/users/basic")
public class EndUsersController {

    @Autowired
    private JwtService authService;

    @Autowired
    private UserGroupAssignmentService userGroupAssignmentService;

    @Autowired
    private com.myservers.backend.servers.services.ServersService serversService;

    @Autowired
    private com.myservers.backend.servers.services.DiscountsService discountsService;


    @CrossOrigin(origins = "*", exposedHeaders = "**")
    @GetMapping("/get_user_details")
    public GeneralResponse getUserDetails() {

        User user = authService.getUser();
        Map<String, Object> userDetails=new HashMap<>();
        userDetails.put("balance",user.getBalance());
        userDetails.put("firstname",user.getFirstname());
        userDetails.put("lastname",user.getLastname());
        userDetails.put("role",user.getRole());
        userDetails.put("email",user.getEmail());

        // Compute highest discount percentage among user's groups (null if none)
        Double userDiscountPercentage = null;
        try {
            List<UserGroup> groups = userGroupAssignmentService.getUserGroups(user.getId());
            if (groups != null && !groups.isEmpty()) {
                userDiscountPercentage = groups.stream()
                        .map(UserGroup::getDiscountPercentage)
                        .filter(dp -> dp != null)
                        .max(Double::compareTo)
                        .orElse(null);
            }
        } catch (Exception e) {
            // keep null if any error occurs
        }
        userDetails.put("user_discount_percentage", userDiscountPercentage);

        return GeneralResponse.builder()
                .status(200L)
                .dataMap(userDetails)
                .build();
    }

    @CrossOrigin(origins = "*", exposedHeaders = "**")
    @GetMapping("/get_user_server_discounts")
    public GeneralResponse getUserServerDiscounts() {
        try {
            User user = authService.getUser();

            // 1) Compute default_discount_percentage (max across user's groups)
            Double defaultDiscount = null;
            try {
                List<UserGroup> groups = userGroupAssignmentService.getUserGroups(user.getId());
                if (groups != null && !groups.isEmpty()) {
                    defaultDiscount = groups.stream()
                            .map(UserGroup::getDiscountPercentage)
                            .filter(dp -> dp != null)
                            .max(Double::compareTo)
                            .orElse(null);
                }
            } catch (Exception ignored) {}

            // 2) Build server_discounts list: only servers with applicable per-server discount
            List<Map<String, Object>> serverDiscounts = new ArrayList<>();
            serversService.getAllServers().forEach(server -> {
                try {
                    var eff = discountsService.computeEffectiveDiscount(user.getId(), server.getId());
                    if (eff.isPresent() && eff.get() != null) {
                        Map<String, Object> entry = new HashMap<>();
                        entry.put("server_id", server.getId());
                        entry.put("server_name", server.getName_serv());
                        entry.put("discount_percentage", eff.get());
                        serverDiscounts.add(entry);
                    }
                } catch (Exception ignored) {}
            });

            Map<String, Object> response = new HashMap<>();
            response.put("default_discount_percentage", defaultDiscount);
            response.put("server_discounts", serverDiscounts);

            return GeneralResponse.builder()
                    .status(200L)
                    .dataMap(response)
                    .build();
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return GeneralResponse.builder()
                    .status(500L)
                    .dataMap(error)
                    .build();
        }
    }

}
