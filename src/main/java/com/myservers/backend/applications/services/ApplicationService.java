package com.myservers.backend.applications.services;

import com.myservers.backend.applications.entities.Application;
import com.myservers.backend.applications.repositories.ApplicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ApplicationService {

    @Autowired
    private ApplicationRepository applicationRepository;

    public List<Application> getAllActiveApplications() {
        return applicationRepository.findByIsActiveTrue();
    }

    public List<Application> getAllApplications() {
        return applicationRepository.findAll();
    }

    public List<Application> getActiveApplications() {
        return applicationRepository.findByIsActiveTrue();
    }

    public List<Application> getApplicationsByCategory(Application.ApplicationCategory category) {
        return applicationRepository.findByCategoryAndIsActiveTrue(category);
    }

    public List<Application> getPopularApplications() {
        return applicationRepository.findByIsPopularTrueAndIsActiveTrue();
    }

    public List<Application> searchApplications(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getAllActiveApplications();
        }
        return applicationRepository.searchActiveApplications(searchTerm.trim());
    }

    public List<Application> searchApplicationsByCategory(Application.ApplicationCategory category, String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getApplicationsByCategory(category);
        }
        return applicationRepository.searchActiveApplicationsByCategory(category, searchTerm.trim());
    }

    public Optional<Application> getApplicationById(Long id) {
        return applicationRepository.findById(id);
    }

    public Application createApplication(Application application) {
        System.out.println("=== DEBUG: ApplicationService.createApplication ===");
        System.out.println("Input application: " + application);
        System.out.println("Category: " + application.getCategory());
        System.out.println("Category type: " + (application.getCategory() != null ? application.getCategory().getClass().getName() : "null"));

        if (applicationRepository.existsByName(application.getName())) {
            throw new RuntimeException("Une application avec ce nom existe déjà");
        }

        Application savedApplication = applicationRepository.save(application);
        System.out.println("Saved application: " + savedApplication);
        return savedApplication;
    }

    public Application updateApplication(Long id, Application applicationDetails) {
        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Application non trouvée"));

        // Vérifier si le nom existe déjà pour une autre application
        if (!application.getName().equals(applicationDetails.getName()) &&
            applicationRepository.existsByName(applicationDetails.getName())) {
            throw new RuntimeException("Une application avec ce nom existe déjà");
        }

        application.setName(applicationDetails.getName());
        application.setDescription(applicationDetails.getDescription());
        application.setVersion(applicationDetails.getVersion());
        application.setSize(applicationDetails.getSize());
        application.setIcon(applicationDetails.getIcon());
        application.setDownloadUrl(applicationDetails.getDownloadUrl());
        application.setCategory(applicationDetails.getCategory());
        application.setPopular(applicationDetails.isPopular());
        application.setRating(applicationDetails.getRating());
        application.setActive(applicationDetails.isActive());

        return applicationRepository.save(application);
    }

    public void deleteApplication(Long id) {
        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Application non trouvée"));
        application.setActive(false);
        applicationRepository.save(application);
    }

    public void incrementDownloadCount(Long id) {
        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Application non trouvée"));
        application.setDownloadCount(application.getDownloadCount() + 1);
        applicationRepository.save(application);
    }

    public List<Application.ApplicationCategory> getAllCategories() {
        return List.of(Application.ApplicationCategory.values());
    }
}
