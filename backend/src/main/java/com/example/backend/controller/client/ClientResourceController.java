package com.example.backend.controller.client;

import com.example.backend.dto.admin.permission.*;
import com.example.backend.security.UserPrincipal;
import com.example.backend.service.admin.AdminPermissionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/observateur/resources")
@CrossOrigin(origins = "*")
public class ClientResourceController {

    private final AdminPermissionService permissionService;

    public ClientResourceController(AdminPermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @GetMapping("/videos")
    public ResponseEntity<List<VideoAssignmentResponse>> getMesVideos(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(permissionService.getVideosAssignedTo(principal.getId()));
    }

    @GetMapping("/ressources")
    public ResponseEntity<List<ResourceAssignmentResponse>> getMesRessources(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(permissionService.getResourcesAssignedTo(principal.getId()));
    }

    @GetMapping("/documents")
    public ResponseEntity<List<DocumentAssignmentResponse>> getMesDocuments(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(permissionService.getDocumentsAssignedTo(principal.getId()));
    }
}