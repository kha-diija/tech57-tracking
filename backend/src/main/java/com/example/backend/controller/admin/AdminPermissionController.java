package com.example.backend.controller.admin;

import com.example.backend.dto.admin.ObservateurSummaryDto;
import com.example.backend.dto.admin.permission.*;
import com.example.backend.security.UserPrincipal;
import com.example.backend.service.admin.AdminPermissionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/permissions")
@CrossOrigin(origins = "*")
public class AdminPermissionController {

    private final AdminPermissionService permissionService;

    public AdminPermissionController(AdminPermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @GetMapping("/observateurs")
    public ResponseEntity<List<ObservateurSummaryDto>> getAllObservateurs() {
        return ResponseEntity.ok(permissionService.getAllObservateurs());
    }

    // ---------- VIDEOS ----------
    @GetMapping("/observateurs/{idObservateur}/videos")
    public ResponseEntity<List<VideoAssignmentResponse>> getVideos(@PathVariable Integer idObservateur) {
        return ResponseEntity.ok(permissionService.getVideosAssignedTo(idObservateur));
    }


    @PostMapping("/videos")
    public ResponseEntity<VideoAssignmentResponse> assignVideo(
            @Valid @RequestBody CreateVideoAssignmentRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        VideoAssignmentResponse created = permissionService.assignVideo(request, principal.getId());
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @DeleteMapping("/videos/{idObservateur}/{idVideo}")
    public ResponseEntity<Void> revokeVideo(@PathVariable Integer idObservateur, @PathVariable Integer idVideo) {
        permissionService.revokeVideo(idObservateur, idVideo);
        return ResponseEntity.noContent().build();
    }

    // ---------- RESSOURCES ----------
    @GetMapping("/observateurs/{idObservateur}/resources")
    public ResponseEntity<List<ResourceAssignmentResponse>> getResources(@PathVariable Integer idObservateur) {
        return ResponseEntity.ok(permissionService.getResourcesAssignedTo(idObservateur));
    }

    @PostMapping("/resources")
    public ResponseEntity<ResourceAssignmentResponse> assignResource(
            @Valid @RequestBody CreateResourceAssignmentRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        ResourceAssignmentResponse created = permissionService.assignResource (request, principal.getId());
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @DeleteMapping("/resources/{idObservateur}/{idRessource}")
    public ResponseEntity<Void> revokeResource(@PathVariable Integer idObservateur, @PathVariable Integer idRessource) {
        permissionService.revokeResource(idObservateur, idRessource);
        return ResponseEntity.noContent().build();
    }

    // ---------- DOCUMENTS ----------
    @GetMapping("/observateurs/{idObservateur}/documents")
    public ResponseEntity<List<DocumentAssignmentResponse>> getDocuments(@PathVariable Integer idObservateur) {
        return ResponseEntity.ok(permissionService.getDocumentsAssignedTo(idObservateur));
    }

    @PostMapping("/documents")
    public ResponseEntity<DocumentAssignmentResponse> assignDocument(
            @Valid @RequestBody CreateDocumentAssignmentRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        DocumentAssignmentResponse created = permissionService.assignDocument (request, principal.getId());
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @DeleteMapping("/documents/{idObservateur}/{idDocument}")
    public ResponseEntity<Void> revokeDocument(@PathVariable Integer idObservateur, @PathVariable Integer idDocument) {
        permissionService.revokeDocument(idObservateur, idDocument);
        return ResponseEntity.noContent().build();
    }
    // ---------- CATALOGUES ----------
    @GetMapping("/catalog/videos")
    public ResponseEntity<List<VideoCatalogDto>> getVideoCatalog() {
        return ResponseEntity.ok(permissionService.getAllVideosCatalog());
    }

    @GetMapping("/catalog/resources")
    public ResponseEntity<List<RessourceCatalogDto>> getResourceCatalog() {
        return ResponseEntity.ok(permissionService.getAllRessourcesCatalog());
    }

    @GetMapping("/catalog/documents")
    public ResponseEntity<List<DocumentCatalogDto>> getDocumentCatalog() {
        return ResponseEntity.ok(permissionService.getAllDocumentsCatalog());
    }
}