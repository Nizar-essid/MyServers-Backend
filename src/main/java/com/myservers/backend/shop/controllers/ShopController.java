package com.myservers.backend.shop.controllers;

import com.myservers.backend.security.config.JwtService;
import com.myservers.backend.servers.classes.GeneralResponse;
import com.myservers.backend.shop.classes.ProductResponse;
import com.myservers.backend.shop.services.ProductService;
import com.myservers.backend.shop.services.ShopAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@CrossOrigin(origins = "*", exposedHeaders = "**")
@RestController
@RequestMapping("/api/v1/shop")
@RequiredArgsConstructor
public class ShopController {

    private final ProductService productService;
    private final ShopAccessService shopAccessService;
    private final JwtService jwtService;

    @GetMapping("/products")
    public GeneralResponse getAvailableProducts() {
        try {
            // Check if user has shop access
            com.myservers.backend.security.auth.entities.User user = jwtService.getUser();
            if (!shopAccessService.hasShopAccess(user)) {
                return GeneralResponse.builder()
                        .status(403L)
                        .result("Access denied. You don't have permission to access the shop.")
                        .build();
            }

            java.util.List<ProductResponse> products = productService.getAllActiveProducts();
            return GeneralResponse.builder()
                    .status(200L)
                    .result("Success")
                    .data(new ArrayList<>(products))
                    .build();
        } catch (Exception e) {
            return GeneralResponse.builder()
                    .status(500L)
                    .result("Error: " + e.getMessage())
                    .build();
        }
    }

    @GetMapping("/check-access")
    public GeneralResponse checkShopAccess() {
        try {
            com.myservers.backend.security.auth.entities.User user = jwtService.getUser();
            boolean hasAccess = shopAccessService.hasShopAccess(user);
            GeneralResponse response = GeneralResponse.builder()
                    .status(200L)
                    .result("Success")
                    .build();
            response.setTrueFalse(hasAccess);
            return response;
        } catch (Exception e) {
            return GeneralResponse.builder()
                    .status(500L)
                    .result("Error: " + e.getMessage())
                    .build();
        }
    }
}

