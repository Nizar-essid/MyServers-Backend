package com.myservers.backend.applications.controllers;

import com.myservers.backend.applications.entities.Application;
import com.myservers.backend.applications.services.ApplicationService;
import com.myservers.backend.applications.services.FileUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/applications")
@CrossOrigin(origins = "*")
public class ApplicationController {

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private FileUploadService fileUploadService;

    @GetMapping
    public ResponseEntity<List<Application>> getAllApplications() {
        List<Application> applications = applicationService.getAllActiveApplications();
        return ResponseEntity.ok(applications);
    }

    @GetMapping("/active")
    public ResponseEntity<List<Application>> getActiveApplications() {
      System.out.println("=== DEBUG: Active applications endpoint called ===============================================================================================================================================================================================================================");
        List<Application> activeApplications = applicationService.getActiveApplications();
        return ResponseEntity.ok(activeApplications);
    }

    @GetMapping("/admin/all")
    public ResponseEntity<List<Application>> getAllApplicationsForAdmin() {
        List<Application> applications = applicationService.getAllApplications();
        System.out.println("=== DEBUG: Admin applications endpoint ===");
        System.out.println("Total applications found: " + applications.size());
        for (Application app : applications) {
            System.out.println("App: " + app.getName() + " - isActive: " + app.isActive());
        }
        System.out.println("=== END DEBUG ===");
        return ResponseEntity.ok(applications);
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<Application>> getApplicationsByCategory(@PathVariable String category) {
        try {
            Application.ApplicationCategory appCategory = Application.ApplicationCategory.valueOf(category.toUpperCase());
            List<Application> applications = applicationService.getApplicationsByCategory(appCategory);
            return ResponseEntity.ok(applications);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/popular")
    public ResponseEntity<List<Application>> getPopularApplications() {
        List<Application> applications = applicationService.getPopularApplications();
        return ResponseEntity.ok(applications);
    }

    @GetMapping("/search")
    public ResponseEntity<List<Application>> searchApplications(@RequestParam String q) {
        List<Application> applications = applicationService.searchApplications(q);
        return ResponseEntity.ok(applications);
    }

    @GetMapping("/search/category/{category}")
    public ResponseEntity<List<Application>> searchApplicationsByCategory(
            @PathVariable String category,
            @RequestParam String q) {
        try {
            Application.ApplicationCategory appCategory = Application.ApplicationCategory.valueOf(category.toUpperCase());
            List<Application> applications = applicationService.searchApplicationsByCategory(appCategory, q);
            return ResponseEntity.ok(applications);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Application> getApplicationById(@PathVariable Long id) {
        return applicationService.getApplicationById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Application> createApplication(@RequestBody Application application) {
        System.out.println("=== DEBUG: Create application endpoint called ===");
        System.out.println("Application data: " + application);
        try {
            // Si la catégorie est une string, la convertir en enum
            if (application.getCategory() == null) {
                System.err.println("Category is null");
                return ResponseEntity.badRequest().build();
            }

            Application createdApplication = applicationService.createApplication(application);
            System.out.println("Application created successfully: " + createdApplication.getId());
            return ResponseEntity.ok(createdApplication);
        } catch (RuntimeException e) {
            System.err.println("Error creating application: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Application> updateApplication(@PathVariable Long id, @RequestBody Application application) {
        try {
            Application updatedApplication = applicationService.updateApplication(id, application);
            return ResponseEntity.ok(updatedApplication);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteApplication(@PathVariable Long id) {
        try {
            applicationService.deleteApplication(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/download")
    public ResponseEntity<Void> incrementDownloadCount(@PathVariable Long id) {
        try {
            applicationService.incrementDownloadCount(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/categories")
    public ResponseEntity<List<Application.ApplicationCategory>> getAllCategories() {
        List<Application.ApplicationCategory> categories = applicationService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    @PostMapping("/upload-icon")
    public ResponseEntity<Map<String, String>> uploadIcon(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        System.out.println("=== DEBUG: Upload icon endpoint called ===");
        System.out.println("Authorization header: " + request.getHeader("Authorization"));
        System.out.println("X-Access-Token header: " + request.getHeader("X-Access-Token"));
        System.out.println("File name: " + file.getOriginalFilename());
        System.out.println("File size: " + file.getSize());

        try {
            String filename = fileUploadService.uploadImage(file);
            Map<String, String> response = new HashMap<>();
            response.put("imagePath", filename);
            response.put("message", "Image uploadée avec succès");
            System.out.println("Upload successful: " + filename);
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            System.err.println("Upload error: " + e.getMessage());
            Map<String, String> response = new HashMap<>();
            response.put("error", "Erreur lors de l'upload de l'image: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/icons/{filename:.+}")
    public ResponseEntity<Resource> serveIcon(@PathVariable String filename) {
        try {
            Path filePath = Paths.get("uploads/applications/icons/").resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
