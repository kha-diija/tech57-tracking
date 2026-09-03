package com.example.backend.controller.admin;

import com.example.backend.dto.admin.intervention.CreateInterventionRequest;
import com.example.backend.dto.admin.intervention.InterventionResponse;
import com.example.backend.dto.admin.intervention.UpdateInterventionRequest;
import com.example.backend.dto.admin.intervention.TechnicienDropdownDto;
import com.example.backend.repository.admin.TechnicienRepository;
import com.example.backend.security.UserPrincipal;
import com.example.backend.service.admin.AttestationPdfService;
import com.example.backend.service.admin.InterventionService;
import com.example.backend.service.admin.RapportPdfService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/interventions")
@CrossOrigin(origins = "*")
public class InterventionController {

    private final InterventionService interventionService;
    private final TechnicienRepository technicienRepository;
    private final RapportPdfService rapportPdfService;
    private final AttestationPdfService attestationPdfService;

    public InterventionController(InterventionService interventionService,
                                  TechnicienRepository technicienRepository,
                                  RapportPdfService rapportPdfService,
                                  AttestationPdfService attestationPdfService) {
        this.interventionService = interventionService;
        this.technicienRepository = technicienRepository;
        this.rapportPdfService = rapportPdfService;
        this.attestationPdfService = attestationPdfService;
    }

    @GetMapping
    public ResponseEntity<List<InterventionResponse>> getAllInterventions() {
        List<InterventionResponse> interventions = interventionService.getAll();
        return ResponseEntity.ok(interventions);
    }

    @Transactional
    @GetMapping("/form-data")
    public ResponseEntity<Map<String, Object>> getFormData() {
        Map<String, Object> formData = new HashMap<>();

        List<TechnicienDropdownDto> techniciens = technicienRepository.findAll().stream()
                .map(t -> new TechnicienDropdownDto(t.getId(), t.getNom(), t.getPrenom()))
                .collect(Collectors.toList());

        formData.put("techniciens", techniciens);
        return ResponseEntity.ok(formData);
    }

    @GetMapping("/{id}")
    public ResponseEntity<InterventionResponse> getInterventionById(@PathVariable Integer id) {
        InterventionResponse intervention = interventionService.getById(id);
        return ResponseEntity.ok(intervention);
    }

    @PostMapping
    public ResponseEntity<InterventionResponse> createIntervention(@Valid @RequestBody CreateInterventionRequest request) {
        InterventionResponse created = interventionService.create(request);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<InterventionResponse> updateIntervention(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateInterventionRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        Integer idAuteur = principal != null ? principal.getId() : null;
        InterventionResponse updated = interventionService.update(id, request, idAuteur);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteIntervention(@PathVariable Integer id) {
        interventionService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/attestation/download")
    public ResponseEntity<byte[]> downloadAttestation(@PathVariable Integer id) {
        try {
            var attestation = interventionService.getAttestationFichier(id);

            // ✅ Cas 1 : Vérifier si une attestation signée a été uploadée (cheminFichierSigne)
            if (attestation != null && attestation.getCheminFichierSigne() != null) {
                Path path = Paths.get("." + attestation.getCheminFichierSigne());
                byte[] fileBytes = Files.readAllBytes(path);
                String filename = path.getFileName().toString();
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                        .contentType(MediaType.APPLICATION_PDF)
                        .body(fileBytes);
            }

            // ✅ Cas 2 : Vérifier si un fichier a été uploadé (ancien champ chemin_fichier)
            if (attestation != null && attestation.getCheminFichier() != null) {
                Path path = Paths.get("." + attestation.getCheminFichier());
                byte[] fileBytes = Files.readAllBytes(path);
                String filename = path.getFileName().toString();
                MediaType mediaType = filename.toLowerCase().endsWith(".pdf")
                        ? MediaType.APPLICATION_PDF
                        : MediaType.IMAGE_JPEG;
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                        .contentType(mediaType)
                        .body(fileBytes);
            }

            // ✅ Cas 3 : aucun fichier uploadé → génération automatique du PDF
            byte[] pdfContent = attestationPdfService.genererAttestationPdf(id);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"attestation_intervention_" + id + ".pdf\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfContent);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}/rapport/download")
    public ResponseEntity<byte[]> downloadRapport(@PathVariable Integer id) {
        try {
            byte[] pdfContent = rapportPdfService.genererRapportPdf(id);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"rapport_intervention_" + id + ".pdf\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfContent);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PatchMapping("/{id}/force-complete")
    public ResponseEntity<InterventionResponse> forceCompleteIntervention(@PathVariable Integer id) {
        InterventionResponse updated = interventionService.forceCompleteByAdmin(id);
        return ResponseEntity.ok(updated);
    }
}