package com.myservers.backend.servers.controllers;

import com.myservers.backend.servers.classes.CategoryResponse;
import com.myservers.backend.servers.classes.GeneralResponse;
import com.myservers.backend.servers.entities.CategoryType;
import com.myservers.backend.servers.services.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@CrossOrigin(origins = "*", exposedHeaders = "**")
@RestController
@RequestMapping("/api/v1/servers/categories")
@RequiredArgsConstructor
public class CategoryClientController {

    private final CategoryService categoryService;

    private static CategoryType parseType(String type) {
        if (type == null || type.isBlank()) return CategoryType.SERVER;
        try {
            return CategoryType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            return CategoryType.SERVER;
        }
    }

    @GetMapping("/tree")
    public GeneralResponse getCategoriesTree(@RequestParam(required = false) String type) {
        try {
            CategoryType categoryType = parseType(type);
            List<CategoryResponse> categories = categoryService.getCategoriesTree(categoryType);
            return GeneralResponse.builder()
                    .status(200L)
                    .result("Success")
                    .data(new ArrayList<>(categories))
                    .build();
        } catch (Exception e) {
            return GeneralResponse.builder()
                    .status(500L)
                    .result("Error: " + e.getMessage())
                    .build();
        }
    }
}

