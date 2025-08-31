package com.myservers.backend.applications.services;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileUploadService {

    private static final String UPLOAD_DIR = "uploads/applications/icons/";

    public String uploadImage(MultipartFile file) throws IOException {
        // Validation du fichier
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Le fichier est vide");
        }

        // Vérifier la taille (max 2MB)
        if (file.getSize() > 2 * 1024 * 1024) {
            throw new IllegalArgumentException("Le fichier est trop volumineux (max 2MB)");
        }

        // Vérifier le type de fichier
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Le fichier doit être une image");
        }

        // Créer le répertoire s'il n'existe pas
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Générer un nom de fichier unique
        String originalFilename = file.getOriginalFilename();
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String filename = UUID.randomUUID().toString() + fileExtension;

        // Sauvegarder le fichier
        Path filePath = uploadPath.resolve(filename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Retourner seulement le nom du fichier (pas le chemin complet)
        return filename;
    }

    public void deleteImage(String imagePath) {
        if (imagePath != null && imagePath.startsWith("/api/v1/applications/icons/")) {
            String filename = imagePath.substring("/api/v1/applications/icons/".length());
            Path filePath = Paths.get(UPLOAD_DIR, filename);
            try {
                Files.deleteIfExists(filePath);
            } catch (IOException e) {
                // Log l'erreur mais ne pas faire échouer l'opération
                System.err.println("Erreur lors de la suppression du fichier: " + e.getMessage());
            }
        }
    }
}
