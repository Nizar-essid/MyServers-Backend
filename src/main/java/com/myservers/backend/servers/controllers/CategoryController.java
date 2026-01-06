package com.myservers.backend.servers.controllers;

import com.myservers.backend.security.config.JwtService;
import com.myservers.backend.security.auth.entities.User;
import com.myservers.backend.servers.classes.CategoryRequest;
import com.myservers.backend.servers.classes.CategoryResponse;
import com.myservers.backend.servers.classes.GeneralResponse;
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

    @GetMapping
    public GeneralResponse getAllCategories() {
        try {
            List<CategoryResponse> categories = categoryService.getAllCategories();
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
    public GeneralResponse getCategoriesTree() {
        try {
            List<CategoryResponse> categories = categoryService.getCategoriesTree();
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
    public GeneralResponse getRootCategories() {
        try {
            List<CategoryResponse> categories = categoryService.getRootCategories();
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
    public GeneralResponse getChildrenCategories(@PathVariable Long id) {
        try {
            List<CategoryResponse> categories = categoryService.getChildrenCategories(id);
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
    public GeneralResponse getLeafCategories() {
        try {
            List<CategoryResponse> categories = categoryService.getLeafCategories();
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

