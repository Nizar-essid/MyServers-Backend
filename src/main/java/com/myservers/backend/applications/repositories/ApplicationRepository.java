package com.myservers.backend.applications.repositories;

import com.myservers.backend.applications.entities.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {

    List<Application> findByIsActiveTrue();

    List<Application> findByCategoryAndIsActiveTrue(Application.ApplicationCategory category);

    List<Application> findByIsPopularTrueAndIsActiveTrue();

    @Query("SELECT a FROM Application a WHERE a.isActive = true AND " +
           "(LOWER(a.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(a.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<Application> searchActiveApplications(@Param("searchTerm") String searchTerm);

    @Query("SELECT a FROM Application a WHERE a.isActive = true AND a.category = :category AND " +
           "(LOWER(a.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(a.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<Application> searchActiveApplicationsByCategory(@Param("category") Application.ApplicationCategory category,
                                                        @Param("searchTerm") String searchTerm);

    boolean existsByName(String name);
}
