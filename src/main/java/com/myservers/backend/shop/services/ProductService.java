package com.myservers.backend.shop.services;

import com.myservers.backend.security.auth.entities.Admin;
import com.myservers.backend.servers.entities.Category;
import com.myservers.backend.servers.entities.CategoryType;
import com.myservers.backend.servers.services.CategoryService;
import com.myservers.backend.shop.classes.ProductRequest;
import com.myservers.backend.shop.classes.ProductResponse;
import com.myservers.backend.shop.entities.Product;
import com.myservers.backend.shop.repositories.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryService categoryService;

    public List<ProductResponse> getAllActiveProducts() {
        return productRepository.findByIsActiveTrueAndIsAvailableTrueOrderByDateCreationDesc()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public List<ProductResponse> getAllProducts() {
        return productRepository.findByIsActiveTrueOrderByDateCreationDesc()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public ProductResponse getProductById(Long id) {
        return productRepository.findById(id)
                .filter(Product::getIsActive)
                .map(this::convertToResponse)
                .orElse(null);
    }

    public ProductResponse createProduct(ProductRequest request, Admin createdBy) {
        Category category = resolveShopCategory(request.getCategoryId());
        Product product = Product.builder()
                .category(category)
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .image(request.getImage())
                .isAvailable(request.getIsAvailable() != null ? request.getIsAvailable() : true)
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .dateCreation(new Date())
                .createdBy(createdBy)
                .build();

        Product saved = productRepository.save(product);
        return convertToResponse(saved);
    }

    public ProductResponse updateProduct(Long id, ProductRequest request, Admin updatedBy) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        product.setCategory(resolveShopCategory(request.getCategoryId()));
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setImage(request.getImage());
        if (request.getIsAvailable() != null) {
            product.setIsAvailable(request.getIsAvailable());
        }
        if (request.getIsActive() != null) {
            product.setIsActive(request.getIsActive());
        }
        product.setLatestUpdate(new Date());
        product.setUpdatedBy(updatedBy);

        Product saved = productRepository.save(product);
        return convertToResponse(saved);
    }

    /** Resolves category for product: must be SHOP type and leaf. Returns null if categoryId is null. */
    private Category resolveShopCategory(Long categoryId) {
        if (categoryId == null) return null;
        Category cat = categoryService.getCategoryByIdEntity(categoryId);
        if (cat.getType() != CategoryType.SHOP) {
            throw new IllegalArgumentException("Product category must be a shop category.");
        }
        if (!categoryService.isLeafCategory(categoryId)) {
            throw new IllegalArgumentException("Only leaf shop categories can contain products.");
        }
        return cat;
    }

    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        product.setIsActive(false);
        productRepository.save(product);
    }

    private ProductResponse convertToResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .image(product.getImage())
                .isAvailable(product.getIsAvailable())
                .isActive(product.getIsActive())
                .dateCreation(product.getDateCreation())
                .latestUpdate(product.getLatestUpdate())
                .createdById(product.getCreatedBy() != null ? Long.valueOf(product.getCreatedBy().getId()) : null)
                .updatedById(product.getUpdatedBy() != null ? Long.valueOf(product.getUpdatedBy().getId()) : null)
                .build();
    }
}

