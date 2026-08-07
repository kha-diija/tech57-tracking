package com.example.backend.controller.admin;

import com.example.backend.dto.admin.intervention.CreateInterventionRequest;
import com.example.backend.dto.admin.intervention.InterventionResponse;
import com.example.backend.dto.admin.intervention.UpdateInterventionRequest;
import com.example.backend.dto.admin.intervention.TechnicienDropdownDto;
import com.example.backend.repository.admin.TechnicienRepository;
import com.example.backend.service.admin.InterventionService;
import com.example.backend.service.admin.RapportPdfService; // <-- NOUVEL IMPORT
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    private final RapportPdfService rapportPdfService; // <-- NOUVEAU SERVICE

    // CONSTRUCTEUR MIS À JOUR
    public InterventionController(InterventionService interventionService,
                                  TechnicienRepository technicienRepository,
                                  RapportPdfService rapportPdfService) {
        this.interventionService = interventionService;
        this.technicienRepository = technicienRepository;
        this.rapportPdfService = rapportPdfService; // <-- INJECTION
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
            @Valid @RequestBody UpdateInterventionRequest request) {
        InterventionResponse updated = interventionService.update(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteIntervention(@PathVariable Integer id) {
        interventionService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ======================================================
    // NOUVEL ENDPOINT : TÉLÉCHARGEMENT DE L'ATTESTATION PDF (EXISTANT)
    // ======================================================
    @GetMapping("/{id}/attestation/download")
    public ResponseEntity<byte[]> downloadAttestation(@PathVariable Integer id) {
        String attestationContent = "ATTESTATION DE RÉALISATION\n";
        attestationContent += "--------------------------------\n";
        attestationContent += "Intervention N° : " + id + "\n";
        attestationContent += "Date de génération : " + java.time.LocalDateTime.now().toString() + "\n";
        attestationContent += "--------------------------------\n";
        attestationContent += "Ce document atteste de la bonne réalisation de l'intervention.\n";
        attestationContent += "Signé électroniquement par le système.\n";

        byte[] contentBytes = attestationContent.getBytes();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"attestation_intervention_" + id + ".txt\"")
                .contentType(MediaType.TEXT_PLAIN)
                .body(contentBytes);
    }

    // ======================================================
    // NOUVEL ENDPOINT : GÉNÉRATION DU RAPPORT PDF (NOUVEAU)
    // ======================================================
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
    // ======================================================
    // ENDPOINT : FORCER LA CLÔTURE (ADMIN)
    // ======================================================
    @PatchMapping("/{id}/force-complete")
    public ResponseEntity<InterventionResponse> forceCompleteIntervention(@PathVariable Integer id) {
        InterventionResponse updated = interventionService.forceCompleteByAdmin(id);
        return ResponseEntity.ok(updated);
    }
}