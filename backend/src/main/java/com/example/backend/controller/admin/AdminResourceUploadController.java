package com.example.backend.controller.admin;

import com.example.backend.dto.admin.upload.CreateVideoRequest;
import com.example.backend.dto.admin.upload.DocumentUploadResponse;
import com.example.backend.dto.admin.upload.RessourceUploadResponse;
import com.example.backend.dto.admin.permission.VideoCatalogDto;
import com.example.backend.entity.VideoMateriel;
import com.example.backend.security.UserPrincipal;
import com.example.backend.service.admin.AdminResourceUploadService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin/resources")
public class AdminResourceUploadController {

    private static final long MAX_FILE_SIZE_MB = 20;

    private final AdminResourceUploadService uploadService;

    public AdminResourceUploadController(AdminResourceUploadService uploadService) {
        this.uploadService = uploadService;
    }

    @PostMapping(value = "/documents", consumes = "multipart/form-data")
    public ResponseEntity<DocumentUploadResponse> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("typeSource") String typeSource,
            @AuthenticationPrincipal UserPrincipal principal) {

        validateFile(file);
        DocumentUploadResponse response = uploadService.uploadDocument(file, typeSource, principal.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping(value = "/ressources", consumes = "multipart/form-data")
    public ResponseEntity<RessourceUploadResponse> uploadRessource(
            @RequestParam("file") MultipartFile file,
            @RequestParam("titre") String titre,
            @RequestParam("type") String type,
            @RequestParam("idEtablissement") Integer idEtablissement) {   // <-- required maintenant

        validateFile(file);
        RessourceUploadResponse response = uploadService.uploadRessource(file, titre, type, idEtablissement);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/videos")
    public ResponseEntity<VideoCatalogDto> createVideo(
            @Valid @RequestBody CreateVideoRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {

        VideoMateriel saved = uploadService.createVideo(request, principal.getId());
        VideoCatalogDto dto = new VideoCatalogDto(
                saved.getIdVideo(), saved.getTitre(), saved.getFournisseur(), saved.getDureeSecondes());
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Aucun fichier fourni.");
        }
        long sizeMb = file.getSize() / (1024 * 1024);
        if (sizeMb > MAX_FILE_SIZE_MB) {
            throw new IllegalArgumentException("Le fichier dépasse la taille maximale de " + MAX_FILE_SIZE_MB + " Mo.");
        }
    }
}