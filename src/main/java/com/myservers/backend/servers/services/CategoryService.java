package com.myservers.backend.servers.services;

import com.myservers.backend.exceptions.ApiRequestException;
import com.myservers.backend.security.auth.entities.Admin;
import com.myservers.backend.security.auth.entities.User;
import com.myservers.backend.servers.classes.CategoryRequest;
import com.myservers.backend.servers.classes.CategoryResponse;
import com.myservers.backend.servers.entities.Category;
import com.myservers.backend.servers.repositories.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    public List<CategoryResponse> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        return categories.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public List<CategoryResponse> getRootCategories() {
        List<Category> rootCategories = categoryRepository.findByParentIsNull();
        return rootCategories.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public List<CategoryResponse> getCategoriesTree() {
        List<Category> rootCategories = categoryRepository.findByParentIsNull();
        return rootCategories.stream()
                .map(this::convertToTreeResponse)
                .collect(Collectors.toList());
    }

    public CategoryResponse getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ApiRequestException("Category not found", HttpStatus.NOT_FOUND));
        return convertToTreeResponse(category);
    }

    public List<CategoryResponse> getChildrenCategories(Long parentId) {
        List<Category> children = categoryRepository.findByParentId(parentId);
        return children.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public List<CategoryResponse> getLeafCategories() {
        List<Category> allCategories = categoryRepository.findAll();
        return allCategories.stream()
                .filter(category -> !categoryRepository.existsByParentId(category.getId()))
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public boolean isLeafCategory(Long categoryId) {
        return !categoryRepository.existsByParentId(categoryId);
    }

    public Category getCategoryByIdEntity(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ApiRequestException("Category not found", HttpStatus.NOT_FOUND));
    }

    @Transactional
    public CategoryResponse createCategory(CategoryRequest request, User user) {
        // Validate parent if provided
        Category parent = null;
        if (request.getParentId() != null) {
            parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ApiRequestException("Parent category not found", HttpStatus.NOT_FOUND));
        }

        // Check if name is unique at the same level
        if (parent != null) {
            boolean nameExists = categoryRepository.findByParentId(parent.getId()).stream()
                    .anyMatch(c -> c.getName().equalsIgnoreCase(request.getName()) && !c.getId().equals(request.getParentId()));
            if (nameExists) {
                throw new ApiRequestException("Category name already exists at this level", HttpStatus.CONFLICT);
            }
        } else {
            boolean nameExists = categoryRepository.findByParentIsNull().stream()
                    .anyMatch(c -> c.getName().equalsIgnoreCase(request.getName()));
            if (nameExists) {
                throw new ApiRequestException("Category name already exists at root level", HttpStatus.CONFLICT);
            }
        }

        Category category = Category.builder()
                .name(request.getName())
                .description(request.getDescription())
                .parent(parent)
                .dateCreation(new Date())
                .latestUpdate(new Date())
                .createdBy((Admin) user)
                .updatedBy((Admin) user)
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .sortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0)
                .build();

        Category saved = categoryRepository.save(category);
        return convertToTreeResponse(saved);
    }

    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest request, User user) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ApiRequestException("Category not found", HttpStatus.NOT_FOUND));

        // Validate parent if changing
        if (request.getParentId() != null && !request.getParentId().equals(category.getParent() != null ? category.getParent().getId() : null)) {
            // Prevent circular reference (category cannot be its own parent or descendant)
            if (request.getParentId().equals(id)) {
                throw new ApiRequestException("Category cannot be its own parent", HttpStatus.BAD_REQUEST);
            }
            if (isDescendant(category.getId(), request.getParentId())) {
                throw new ApiRequestException("Category cannot be a descendant of itself", HttpStatus.BAD_REQUEST);
            }

            Category newParent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ApiRequestException("Parent category not found", HttpStatus.NOT_FOUND));
            category.setParent(newParent);
        }

        // Check if name is unique at the same level (excluding current category)
        Category parent = category.getParent();
        if (parent != null) {
            boolean nameExists = categoryRepository.findByParentId(parent.getId()).stream()
                    .anyMatch(c -> c.getName().equalsIgnoreCase(request.getName()) && !c.getId().equals(id));
            if (nameExists) {
                throw new ApiRequestException("Category name already exists at this level", HttpStatus.CONFLICT);
            }
        } else {
            boolean nameExists = categoryRepository.findByParentIsNull().stream()
                    .anyMatch(c -> c.getName().equalsIgnoreCase(request.getName()) && !c.getId().equals(id));
            if (nameExists) {
                throw new ApiRequestException("Category name already exists at root level", HttpStatus.CONFLICT);
            }
        }

        if (request.getName() != null) {
            category.setName(request.getName());
        }
        if (request.getDescription() != null) {
            category.setDescription(request.getDescription());
        }
        if (request.getIsActive() != null) {
            category.setIsActive(request.getIsActive());
        }
        if (request.getSortOrder() != null) {
            category.setSortOrder(request.getSortOrder());
        }

        category.setLatestUpdate(new Date());
        category.setUpdatedBy((Admin) user);

        Category updated = categoryRepository.save(category);
        return convertToTreeResponse(updated);
    }

    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ApiRequestException("Category not found", HttpStatus.NOT_FOUND));

        // Check if category has children
        if (categoryRepository.existsByParentId(id)) {
            throw new ApiRequestException("Cannot delete category with children. Please delete children first.", HttpStatus.BAD_REQUEST);
        }

        // TODO: In future, check if category has servers attached
        // For now, just delete if no children

        categoryRepository.delete(category);
    }

    private boolean isDescendant(Long categoryId, Long potentialAncestorId) {
        Category category = categoryRepository.findById(categoryId).orElse(null);
        if (category == null) return false;

        Category current = category.getParent();
        while (current != null) {
            if (current.getId().equals(potentialAncestorId)) {
                return true;
            }
            current = current.getParent();
        }
        return false;
    }

    private CategoryResponse convertToResponse(Category category) {
        CategoryResponse response = CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .parentId(category.getParent() != null ? category.getParent().getId() : null)
                .parentName(category.getParent() != null ? category.getParent().getName() : null)
                .dateCreation(category.getDateCreation())
                .latestUpdate(category.getLatestUpdate())
                .createdById(category.getCreatedBy() != null ? category.getCreatedBy().getId() : null)
                .updatedById(category.getUpdatedBy() != null ? category.getUpdatedBy().getId() : null)
                .isActive(category.getIsActive())
                .sortOrder(category.getSortOrder())
                .hasChildren(categoryRepository.existsByParentId(category.getId()))
                .childrenCount(categoryRepository.countChildrenByParentId(category.getId()))
                .build();

        // Load children if needed
        List<Category> children = categoryRepository.findByParentId(category.getId());
        if (children != null && !children.isEmpty()) {
            response.setChildren(children.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList()));
        }

        return response;
    }

    private CategoryResponse convertToTreeResponse(Category category) {
        CategoryResponse response = convertToResponse(category);

        // Recursively load all children
        List<Category> children = categoryRepository.findByParentId(category.getId());
        if (children != null && !children.isEmpty()) {
            response.setChildren(children.stream()
                    .map(this::convertToTreeResponse)
                    .collect(Collectors.toList()));
        } else {
            response.setChildren(null);
        }

        return response;
    }
}

