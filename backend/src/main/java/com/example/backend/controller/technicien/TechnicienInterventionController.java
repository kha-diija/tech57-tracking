package com.example.backend.controller.technicien;

import com.example.backend.dto.admin.intervention.CreateInterventionRequest;
import com.example.backend.dto.admin.intervention.InterventionResponse;
import com.example.backend.dto.admin.intervention.UpdateInterventionRequest;
import com.example.backend.dto.technicien.Dashboard.AttestationPreviewRequest;
import com.example.backend.dto.technicien.Dashboard.CheckInRequest;
import com.example.backend.dto.technicien.Dashboard.CheckOutRequest;
import com.example.backend.dto.technicien.Dashboard.MissionSimplifieeDTO;
import com.example.backend.entity.Materiel;
import com.example.backend.repository.admin.MaterielRepository;
import com.example.backend.security.UserPrincipal;
import com.example.backend.service.admin.AttestationPdfService;
import com.example.backend.service.technicien.TechnicienInterventionService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.example.backend.dto.technicien.MaterielSimpleDto;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/technicien/interventions")
@CrossOrigin(origins = "*")
public class TechnicienInterventionController {

    private final TechnicienInterventionService technicienService;
    private final MaterielRepository materielRepository;
    private final AttestationPdfService attestationPdfService;

    public TechnicienInterventionController(TechnicienInterventionService technicienService,
                                            MaterielRepository materielRepository,
                                            AttestationPdfService attestationPdfService) {
        this.technicienService = technicienService;
        this.materielRepository = materielRepository;
        this.attestationPdfService = attestationPdfService;
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

            byte[] pdfContent = technicienService.genererAttestation(id);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"attestation_intervention_" + id + ".pdf\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfContent);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // ✅ NOUVEAU ENDPOINT : Générer l'attestation à signer
    @GetMapping("/{id}/attestation/generate")
    public ResponseEntity<byte[]> generateAttestation(
            @PathVariable Integer id,
            @AuthenticationPrincipal UserPrincipal principal) {
        try {
            technicienService.getInterventionById(id, principal.getId());

            byte[] pdfBytes = attestationPdfService.genererAttestationPdf(id);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, "application/pdf")
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"attestation_a_signer_" + id + ".pdf\"")
                    .body(pdfBytes);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ✅ NOUVEAU ENDPOINT : Uploader l'attestation signée
    @PostMapping(value = "/{id}/attestation/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadAttestationSignee(
            @PathVariable Integer id,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserPrincipal principal) {
        try {
            technicienService.getInterventionById(id, principal.getId());

            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Le fichier est vide"));
            }

            // Créer le dossier d'upload
            String uploadDir = "uploads/attestations/";
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Générer un nom de fichier unique
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : ".pdf";
            String fileName = UUID.randomUUID().toString() + "_attestation_signe_" + id + extension;
            Path filePath = uploadPath.resolve(fileName);

            // Sauvegarder le fichier
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Mettre à jour l'attestation en base
            String cheminFichierSigne = "/uploads/attestations/" + fileName;
            attestationPdfService.uploadAttestationSignee(id, cheminFichierSigne);

            return ResponseEntity.ok().body(Map.of(
                    "message", "Attestation signée uploadée avec succès",
                    "chemin", cheminFichierSigne
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ✅ NOUVEAU ENDPOINT : Télécharger l'attestation signée
    @GetMapping("/{id}/attestation/signee/download")
    public ResponseEntity<byte[]> downloadAttestationSignee(
            @PathVariable Integer id,
            @AuthenticationPrincipal UserPrincipal principal) {
        try {
            technicienService.getInterventionById(id, principal.getId());

            String chemin = technicienService.getAttestationSigneePath(id);
            if (chemin == null || chemin.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Path filePath = Paths.get("." + chemin);
            if (!Files.exists(filePath)) {
                return ResponseEntity.notFound().build();
            }

            byte[] fileBytes = Files.readAllBytes(filePath);
            String filename = filePath.getFileName().toString();

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, "application/pdf")
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + filename + "\"")
                    .body(fileBytes);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ✅ NOUVEAU : génère un PDF "attestation à signer" à partir des données
    // actuellement saisies dans le modal de checkout (pas encore persistées).
    @PostMapping(value = "/{id}/attestation/preview", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<byte[]> downloadAttestationPreview(
            @PathVariable Integer id,
            @RequestBody AttestationPreviewRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        try {
            byte[] pdf = technicienService.genererAttestationPreview(id, principal.getId(), request);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"attestation_a_signer_intervention_" + id + ".pdf\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdf);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}