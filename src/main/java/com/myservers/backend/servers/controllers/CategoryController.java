package com.myservers.backend.servers.controllers;

import com.myservers.backend.security.config.JwtService;
import com.myservers.backend.security.auth.entities.User;
import com.myservers.backend.servers.classes.CategoryRequest;
import com.myservers.backend.servers.classes.CategoryResponse;
import com.myservers.backend.servers.classes.GeneralResponse;
import com.myservers.backend.servers.entities.CategoryType;
import com.myservers.backend.servers.services.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@CrossOrigin(origins = "*", exposedHeaders = "**")
@RestController
@RequestMapping("/api/v1/servers/admin/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;
    private final JwtService jwtService;

    private static CategoryType parseType(String type) {
        if (type == null || type.isBlank()) return CategoryType.SERVER;
        try {
            return CategoryType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            return CategoryType.SERVER;
        }
    }

    @GetMapping
    public GeneralResponse getAllCategories(@RequestParam(required = false) String type) {
        try {
            CategoryType categoryType = parseType(type);
            List<CategoryResponse> categories = categoryService.getAllCategories(categoryType);
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

    @GetMapping("/roots")
    public GeneralResponse getRootCategories(@RequestParam(required = false) String type) {
        try {
            CategoryType categoryType = parseType(type);
            List<CategoryResponse> categories = categoryService.getRootCategories(categoryType);
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

    @GetMapping("/{id}")
    public GeneralResponse getCategoryById(@PathVariable Long id) {
        try {
            CategoryResponse category = categoryService.getCategoryById(id);
            ArrayList<Object> data = new ArrayList<>();
            data.add(category);
            return GeneralResponse.builder()
                    .status(200L)
                    .result("Success")
                    .data(data)
                    .build();
        } catch (Exception e) {
            return GeneralResponse.builder()
                    .status(404L)
                    .result("Error: " + e.getMessage())
                    .build();
        }
    }

    @GetMapping("/{id}/children")
    public GeneralResponse getChildrenCategories(@PathVariable Long id, @RequestParam(required = false) String type) {
        try {
            CategoryType categoryType = parseType(type);
            List<CategoryResponse> categories = categoryService.getChildrenCategories(id, categoryType);
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

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public GeneralResponse createCategory(@RequestBody CategoryRequest request) {
        try {
            User user = jwtService.getUser();
            CategoryResponse category = categoryService.createCategory(request, user);
            ArrayList<Object> data = new ArrayList<>();
            data.add(category);
            return GeneralResponse.builder()
                    .status(200L)
                    .result("Category created successfully")
                    .data(data)
                    .build();
        } catch (Exception e) {
            return GeneralResponse.builder()
                    .status(500L)
                    .result("Error: " + e.getMessage())
                    .build();
        }
    }

    @PostMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public GeneralResponse updateCategory(@PathVariable Long id, @RequestBody CategoryRequest request) {
        try {
            User user = jwtService.getUser();
            CategoryResponse category = categoryService.updateCategory(id, request, user);
            ArrayList<Object> data = new ArrayList<>();
            data.add(category);
            return GeneralResponse.builder()
                    .status(200L)
                    .result("Category updated successfully")
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
    public GeneralResponse deleteCategory(@PathVariable Long id) {
        try {
            categoryService.deleteCategory(id);
            return GeneralResponse.builder()
                    .status(200L)
                    .result("Category deleted successfully")
                    .build();
        } catch (Exception e) {
            return GeneralResponse.builder()
                    .status(500L)
                    .result("Error: " + e.getMessage())
                    .build();
        }
    }

    @GetMapping("/leaves")
    public GeneralResponse getLeafCategories(@RequestParam(required = false) String type) {
        try {
            CategoryType categoryType = parseType(type);
            List<CategoryResponse> categories = categoryService.getLeafCategories(categoryType);
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

