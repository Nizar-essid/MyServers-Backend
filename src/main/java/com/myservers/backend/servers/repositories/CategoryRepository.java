package com.myservers.backend.servers.repositories;

import com.myservers.backend.servers.entities.Category;
import com.myservers.backend.servers.entities.CategoryType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByParentIsNull();
    List<Category> findByParentIsNullAndType(CategoryType type);

    List<Category> findByParentId(Long parentId);
    List<Category> findByParentIdAndType(Long parentId, CategoryType type);
    List<Category> findByParentIdAndIsActive(Long parentId, Boolean isActive);

    List<Category> findByType(CategoryType type);
    List<Category> findByIsActive(Boolean isActive);
    Optional<Category> findByIdAndIsActive(Long id, Boolean isActive);

    boolean existsByParentId(Long parentId);

    @Query("SELECT COUNT(c) FROM Category c WHERE c.parent.id = :parentId")
    long countChildrenByParentId(@Param("parentId") Long parentId);
}

