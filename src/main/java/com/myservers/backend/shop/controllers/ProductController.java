package com.myservers.backend.shop.controllers;

import com.myservers.backend.security.config.JwtService;
import com.myservers.backend.servers.classes.GeneralResponse;
import com.myservers.backend.shop.classes.ProductRequest;
import com.myservers.backend.shop.classes.ProductResponse;
import com.myservers.backend.shop.services.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@CrossOrigin(origins = "*", exposedHeaders = "**")
@RestController
@RequestMapping("/api/v1/shop/admin/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final JwtService jwtService;

    @GetMapping
    public GeneralResponse getAllProducts() {
        try {
            List<ProductResponse> products = productService.getAllProducts();
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

    @GetMapping("/{id}")
    public GeneralResponse getProductById(@PathVariable Long id) {
        try {
            ProductResponse product = productService.getProductById(id);
            if (product == null) {
                return GeneralResponse.builder()
                        .status(404L)
                        .result("Product not found")
                        .build();
            }
            ArrayList<Object> data = new ArrayList<>();
            data.add(product);
            return GeneralResponse.builder()
                    .status(200L)
                    .result("Success")
                    .data(data)
                    .build();
        } catch (Exception e) {
            return GeneralResponse.builder()
                    .status(500L)
                    .result("Error: " + e.getMessage())
                    .build();
        }
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public GeneralResponse createProduct(@RequestBody ProductRequest request) {
        try {
            com.myservers.backend.security.auth.entities.Admin admin = jwtService.getAdmin();
            ProductResponse product = productService.createProduct(request, admin);
            ArrayList<Object> data = new ArrayList<>();
            data.add(product);
            return GeneralResponse.builder()
                    .status(200L)
                    .result("Product created successfully")
                    .data(data)
                    .build();
        } catch (Exception e) {
          throw new RuntimeException(e);
//            return GeneralResponse.builder()
//                    .status(500L)
//                    .result("Error: " + e.getMessage())
//                    .build();

        }
    }

    @PostMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public GeneralResponse updateProduct(@PathVariable Long id, @RequestBody ProductRequest request) {
        try {
            com.myservers.backend.security.auth.entities.Admin admin = jwtService.getAdmin();
            ProductResponse product = productService.updateProduct(id, request, admin);
            ArrayList<Object> data = new ArrayList<>();
            data.add(product);
            return GeneralResponse.builder()
                    .status(200L)
                    .result("Product updated successfully")
                    .data(data)
                    .build();
        } catch (Exception e) {
            return GeneralResponse.builder()
                    .status(500L)
                    .result("Error: " + e.getMessage())
                    .build();
        }
    }

    @DeleteMapping("/{id}")
    public GeneralResponse deleteProduct(@PathVariable Long id) {
        try {
            productService.deleteProduct(id);
            return GeneralResponse.builder()
                    .status(200L)
                    .result("Product deleted successfully")
                    .build();
        } catch (Exception e) {
            return GeneralResponse.builder()
                    .status(500L)
                    .result("Error: " + e.getMessage())
                    .build();
        }
    }
}

