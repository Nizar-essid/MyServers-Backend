package com.myservers.backend.servers.services;

import com.myservers.backend.exceptions.ApiRequestException;
import com.myservers.backend.security.auth.entities.Admin;
import com.myservers.backend.security.auth.entities.User;
import com.myservers.backend.servers.classes.CategoryRequest;
import com.myservers.backend.servers.classes.CategoryResponse;
import com.myservers.backend.servers.entities.Category;
import com.myservers.backend.servers.entities.CategoryType;
import com.myservers.backend.servers.repositories.CategoryRepository;
import com.myservers.backend.servers.repositories.ServerRepository;
import com.myservers.backend.shop.repositories.ProductRepository;
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

    @Autowired
    private ServerRepository serverRepository;

    @Autowired
    private ProductRepository productRepository;

    public List<CategoryResponse> getAllCategories(CategoryType type) {
        List<Category> categories = type != null ? categoryRepository.findByType(type) : categoryRepository.findAll();
        return categories.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public List<CategoryResponse> getRootCategories(CategoryType type) {
        List<Category> rootCategories = type != null ? categoryRepository.findByParentIsNullAndType(type) : categoryRepository.findByParentIsNull();
        return rootCategories.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public List<CategoryResponse> getCategoriesTree(CategoryType type) {
        List<Category> rootCategories = type != null ? categoryRepository.findByParentIsNullAndType(type) : categoryRepository.findByParentIsNull();
        return rootCategories.stream()
                .map(this::convertToTreeResponse)
                .collect(Collectors.toList());
    }

    public CategoryResponse getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ApiRequestException("Category not found", HttpStatus.NOT_FOUND));
        return convertToTreeResponse(category);
    }

    public List<CategoryResponse> getChildrenCategories(Long parentId, CategoryType type) {
        List<Category> children = type != null ? categoryRepository.findByParentIdAndType(parentId, type) : categoryRepository.findByParentId(parentId);
        return children.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public List<CategoryResponse> getLeafCategories(CategoryType type) {
        List<Category> allCategories = type != null ? categoryRepository.findByType(type) : categoryRepository.findAll();
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
        CategoryType categoryType = request.getType() != null ? request.getType() : CategoryType.SERVER;
        // Validate parent if provided
        Category parent = null;
        if (request.getParentId() != null) {
            parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ApiRequestException("Parent category not found", HttpStatus.NOT_FOUND));
            if (parent.getType() != categoryType) {
                throw new ApiRequestException("Parent category must be of the same type (SERVER or SHOP)", HttpStatus.BAD_REQUEST);
            }
        }

        // Check if name is unique at the same level (same type)
        if (parent != null) {
            boolean nameExists = categoryRepository.findByParentIdAndType(parent.getId(), categoryType).stream()
                    .anyMatch(c -> c.getName().equalsIgnoreCase(request.getName()));
            if (nameExists) {
                throw new ApiRequestException("Category name already exists at this level", HttpStatus.CONFLICT);
            }
        } else {
            boolean nameExists = categoryRepository.findByParentIsNullAndType(categoryType).stream()
                    .anyMatch(c -> c.getName().equalsIgnoreCase(request.getName()));
            if (nameExists) {
                throw new ApiRequestException("Category name already exists at root level", HttpStatus.CONFLICT);
            }
        }

        Category category = Category.builder()
                .type(categoryType)
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

        // Validate parent if changing (must be same type)
        if (request.getParentId() != null && !request.getParentId().equals(category.getParent() != null ? category.getParent().getId() : null)) {
            if (request.getParentId().equals(id)) {
                throw new ApiRequestException("Category cannot be its own parent", HttpStatus.BAD_REQUEST);
            }
            if (isDescendant(category.getId(), request.getParentId())) {
                throw new ApiRequestException("Category cannot be a descendant of itself", HttpStatus.BAD_REQUEST);
            }
            Category newParent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ApiRequestException("Parent category not found", HttpStatus.NOT_FOUND));
            if (newParent.getType() != category.getType()) {
                throw new ApiRequestException("Parent category must be of the same type (SERVER or SHOP)", HttpStatus.BAD_REQUEST);
            }
            category.setParent(newParent);
        }

        Category parent = category.getParent();
        if (parent != null) {
            boolean nameExists = categoryRepository.findByParentIdAndType(parent.getId(), category.getType()).stream()
                    .anyMatch(c -> c.getName().equalsIgnoreCase(request.getName()) && !c.getId().equals(id));
            if (nameExists) {
                throw new ApiRequestException("Category name already exists at this level", HttpStatus.CONFLICT);
            }
        } else {
            boolean nameExists = categoryRepository.findByParentIsNullAndType(category.getType()).stream()
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

        if (categoryRepository.existsByParentId(id)) {
            throw new ApiRequestException("Cannot delete category with children. Please delete children first.", HttpStatus.BAD_REQUEST);
        }

        if (category.getType() == CategoryType.SERVER) {
            long serverCount = serverRepository.countByCategory_Id(id);
            if (serverCount > 0) {
                throw new ApiRequestException("Cannot delete category: " + serverCount + " server(s) are attached. Remove or reassign them first.", HttpStatus.BAD_REQUEST);
            }
        } else if (category.getType() == CategoryType.SHOP) {
            long productCount = productRepository.countByCategory_Id(id);
            if (productCount > 0) {
                throw new ApiRequestException("Cannot delete category: " + productCount + " product(s) are attached. Remove or reassign them first.", HttpStatus.BAD_REQUEST);
            }
        }

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
                .type(category.getType())
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

        // Load children if needed (same type)
        List<Category> children = categoryRepository.findByParentIdAndType(category.getId(), category.getType());
        if (children != null && !children.isEmpty()) {
            response.setChildren(children.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList()));
        }

        return response;
    }

    private CategoryResponse convertToTreeResponse(Category category) {
        CategoryResponse response = convertToResponse(category);

        // Recursively load all children (same type)
        List<Category> children = categoryRepository.findByParentIdAndType(category.getId(), category.getType());
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

