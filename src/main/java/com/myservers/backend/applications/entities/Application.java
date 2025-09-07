package com.myservers.backend.applications.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Table(name = "applications")
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private String version;

    @Column(nullable = false)
    private String size;

    @Column(nullable = false)
    private String icon;

    @Column(nullable = false,length = 550)
    private String downloadUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @JsonProperty("category")
    private ApplicationCategory category;

    @Column(nullable = false)
    private boolean isPopular = false;

    @Column(nullable = false)
    private Long downloadCount = 0L;

    @Column(nullable = false)
    private Double rating = 0.0;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(nullable = true)
    private LocalDateTime createdAt;

    @Column(nullable = true)
    private LocalDateTime updatedAt;

    public enum ApplicationCategory {
        STREAMING("Streaming"),
        SOCIAL_MEDIA("Réseaux sociaux"),
        GAMING("Jeux"),
        PRODUCTIVITY("Productivité"),
        SECURITY("Sécurité");

        private final String displayName;

        ApplicationCategory(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // Constructors
    public Application() {
        // Don't set dates in constructor to allow database defaults
    }

    public Application(String name, String description, String version, String size,
                      String icon, String downloadUrl, ApplicationCategory category) {
        this.name = name;
        this.description = description;
        this.version = version;
        this.size = size;
        this.icon = icon;
        this.downloadUrl = downloadUrl;
        this.category = category;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public ApplicationCategory getCategory() {
        return category;
    }

    public void setCategory(ApplicationCategory category) {
        this.category = category;
    }

    public boolean isPopular() {
        return isPopular;
    }

    public void setPopular(boolean popular) {
        isPopular = popular;
    }

    public Long getDownloadCount() {
        return downloadCount;
    }

    public void setDownloadCount(Long downloadCount) {
        this.downloadCount = downloadCount;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    @JsonProperty("isActive")
    public boolean isActive() {
        return isActive;
    }

    @JsonProperty("isActive")
    public void setActive(boolean active) {
        isActive = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.updatedAt == null) {
            this.updatedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    public void preUpdate() {
        if (this.updatedAt == null) {
            this.updatedAt = LocalDateTime.now();
        }
    }

    @Override
    public String toString() {
        return "Application{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", version='" + version + '\'' +
                ", size='" + size + '\'' +
                ", icon='" + icon + '\'' +
                ", downloadUrl='" + downloadUrl + '\'' +
                ", category=" + category +
                ", isPopular=" + isPopular +
                ", downloadCount=" + downloadCount +
                ", rating=" + rating +
                ", isActive=" + isActive +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
