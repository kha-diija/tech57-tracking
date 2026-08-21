package com.example.backend.service.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path rootLocation;

    public FileStorageService(@Value("${app.upload.dir}") String uploadDir) {
        this.rootLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Impossible de créer le dossier d'upload : " + rootLocation, e);
        }
    }

    /**
     * Stocke un fichier dans un sous-dossier (ex: "documents", "ressources")
     * avec un nom UUID pour éviter les collisions et masquer le nom réel.
     *
     * @return le chemin relatif à exposer/stocker en DB, ex: "/uploads/documents/uuid.pdf"
     */
    public String store(MultipartFile file, String subFolder) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Le fichier est vide.");
        }

        String originalFilename = StringUtils.cleanPath(
                file.getOriginalFilename() != null ? file.getOriginalFilename() : "fichier"
        );
        String extension = "";
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex >= 0) {
            extension = originalFilename.substring(dotIndex);
        }

        String uniqueName = UUID.randomUUID() + extension;

        try {
            Path targetDir = rootLocation.resolve(subFolder).normalize();
            Files.createDirectories(targetDir);

            Path targetPath = targetDir.resolve(uniqueName).normalize();

            // Sécurité : empêche path traversal
            if (!targetPath.getParent().equals(targetDir)) {
                throw new IllegalArgumentException("Chemin de fichier invalide.");
            }

            try (InputStream in = file.getInputStream()) {
                Files.copy(in, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }

            // Chemin relatif servi via /uploads/**
            return "/uploads/" + subFolder + "/" + uniqueName;

        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de l'enregistrement du fichier.", e);
        }
    }

    public long getSizeInMb(MultipartFile file) {
        return file.getSize() / (1024 * 1024);
    }
}