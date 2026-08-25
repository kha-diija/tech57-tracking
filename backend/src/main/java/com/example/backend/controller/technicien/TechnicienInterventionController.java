package com.example.backend.controller.technicien;

import com.example.backend.dto.admin.intervention.CreateInterventionRequest;
import com.example.backend.dto.admin.intervention.InterventionResponse;
import com.example.backend.dto.admin.intervention.UpdateInterventionRequest;
import com.example.backend.dto.technicien.Dashboard.CheckInRequest;
import com.example.backend.dto.technicien.Dashboard.CheckOutRequest;
import com.example.backend.dto.technicien.Dashboard.MissionSimplifieeDTO;
import com.example.backend.entity.Materiel;
import com.example.backend.repository.admin.MaterielRepository;
import com.example.backend.security.UserPrincipal;
import com.example.backend.service.technicien.TechnicienInterventionService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.example.backend.dto.technicien.MaterielSimpleDto;

import java.util.List;

@RestController
@RequestMapping("/api/technicien/interventions")
@CrossOrigin(origins = "*")
public class TechnicienInterventionController {

    private final TechnicienInterventionService technicienService;
    private final MaterielRepository materielRepository;

    public TechnicienInterventionController(TechnicienInterventionService technicienService,
                                            MaterielRepository materielRepository) {
        this.technicienService = technicienService;
        this.materielRepository = materielRepository;
    }

    @GetMapping
    public ResponseEntity<List<InterventionResponse>> getMesInterventions(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(technicienService.getMesInterventions(principal.getId()));
    }

    @GetMapping("/missions")
    public ResponseEntity<List<MissionSimplifieeDTO>> getMissionsForTechnicien(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(technicienService.getMissionsForTechnicien(principal.getId()));
    }

    @GetMapping("/materiels")
    public ResponseEntity<List<MaterielSimpleDto>> getMaterielsForTechnicien() {
        List<MaterielSimpleDto> result = materielRepository.findAll().stream()
                .map(m -> new MaterielSimpleDto(m.getIdMateriel(), m.getNom(), m.getReference()))
                .toList();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<InterventionResponse> getInterventionById(
            @PathVariable Integer id,
            @AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(technicienService.getInterventionById(id, principal.getId()));
    }

    @PostMapping
    public ResponseEntity<InterventionResponse> createIntervention(
            @RequestBody CreateInterventionRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(technicienService.createIntervention(principal.getId(), request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<InterventionResponse> updateIntervention(
            @PathVariable Integer id,
            @RequestBody UpdateInterventionRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(technicienService.updateIntervention(id, principal.getId(), request));
    }

    @PostMapping("/{id}/check-in")
    public ResponseEntity<InterventionResponse> checkIn(
            @PathVariable Integer id,
            @RequestBody CheckInRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(technicienService.checkIn(id, principal.getId(), request));
    }

    // --- CORRECTION : Utilisation de @RequestBody au lieu de @RequestPart ---
    // APRÈS
    @PostMapping(value = "/{id}/check-out", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<InterventionResponse> checkOut(
            @PathVariable Integer id,
            @ModelAttribute CheckOutRequest request,
            @RequestParam(value = "checklist", required = false) String checklistJson,
            @AuthenticationPrincipal UserPrincipal principal) throws Exception {
        return ResponseEntity.ok(technicienService.checkOut(id, principal.getId(), request, checklistJson));
    }

    @GetMapping("/{id}/rapport/download")
    public ResponseEntity<byte[]> downloadRapport(@PathVariable Integer id, @AuthenticationPrincipal UserPrincipal principal) {
        try {
            byte[] pdf = technicienService.genererRapport(id);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"rapport_intervention_" + id + ".pdf\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdf);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}/attestation/download")
    public ResponseEntity<byte[]> downloadAttestation(@PathVariable Integer id, @AuthenticationPrincipal UserPrincipal principal) {
        try {
            var attestation = technicienService.getAttestationFichier(id);

            // Cas 1 : fichier uploadé → on le sert tel quel
            if (attestation != null && attestation.getCheminFichier() != null) {
                java.nio.file.Path path = java.nio.file.Paths.get("." + attestation.getCheminFichier());
                byte[] fileBytes = java.nio.file.Files.readAllBytes(path);
                String filename = path.getFileName().toString();
                MediaType mediaType = filename.toLowerCase().endsWith(".pdf")
                        ? MediaType.APPLICATION_PDF
                        : MediaType.IMAGE_JPEG;
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                        .contentType(mediaType)
                        .body(fileBytes);
            }

            // Cas 2 : pas de fichier → génération automatique du PDF, sans signature requise
            byte[] pdfContent = technicienService.genererAttestation(id);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"attestation_intervention_" + id + ".pdf\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfContent);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}