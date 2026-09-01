package com.example.backend.service.admin;

import com.example.backend.dto.admin.intervention.CheckInOutDto;
import com.example.backend.dto.admin.intervention.InterventionResponse;
import com.example.backend.entity.Attestation;
import com.example.backend.entity.Etablissement;
import com.example.backend.entity.Intervention;
import com.example.backend.repository.admin.AttestationRepository;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class AttestationPdfService {

    private final AttestationRepository attestationRepository;
    private final InterventionService interventionService;

    public AttestationPdfService(AttestationRepository attestationRepository,
                                 InterventionService interventionService) {
        this.attestationRepository = attestationRepository;
        this.interventionService = interventionService;
    }

    public byte[] genererAttestationPdf(Integer interventionId) throws Exception {
        InterventionResponse response = interventionService.getById(interventionId);
        Intervention intervention = interventionService.getInterventionEntity(interventionId);
        Etablissement etablissement = intervention.getMission().getEtablissement();
        String province = etablissement.getCommune().getProvince().getNom();

        String templatePath = getTemplatePath(province);

        if (templatePath == null) {
            throw new RuntimeException("Aucun template d'attestation trouvé pour la province : " + province);
        }

        byte[] pdfBytes = remplirTemplate(templatePath, response, etablissement, province);

        sauvegarderAttestation(interventionId, response, etablissement);

        return pdfBytes;
    }

    private String getTemplatePath(String province) {
        if (province == null) return null;

        String nom = province.toLowerCase();
        if (nom.contains("سيدي قاسم") || nom.contains("sidi kacem")) {
            return "attestations/templates/SidiKacemAttestation.pdf";
        } else if (nom.contains("برشيد") || nom.contains("berrechid")) {
            return "attestations/templates/berrechidAttestation.pdf";
        } else if (nom.contains("تاونات") || nom.contains("taounate")) {
            return "attestations/templates/taounateAttestaion.pdf";
        }
        return null;
    }

    private byte[] remplirTemplate(String templatePath,
                                   InterventionResponse response,
                                   Etablissement etablissement,
                                   String province) throws Exception {

        ClassPathResource resource = new ClassPathResource(templatePath);
        InputStream inputStream = resource.getInputStream();
        PdfReader reader = new PdfReader(inputStream);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfStamper stamper = new PdfStamper(reader, baos);

        // Récupérer les données
        String responsable = etablissement.getResponsable() != null ?
                etablissement.getResponsable().getPrenom() + " " + etablissement.getResponsable().getNom() : "";
        String nomEtablissement = etablissement.getDesignation() != null ? etablissement.getDesignation() : "";
        String commune = etablissement.getCommune().getNom() != null ? etablissement.getCommune().getNom() : "";
        String dateArrivee = getFirstCheckinDate(response);
        String dateDepart = getLastCheckoutDate(response);
        String nbBeneficiaires = etablissement.getNombreBeneficiairesReel() != null ?
                String.valueOf(etablissement.getNombreBeneficiairesReel()) : "0";

        BaseFont baseFont = BaseFont.createFont(
                BaseFont.HELVETICA,
                BaseFont.WINANSI,
                BaseFont.EMBEDDED
        );

        PdfContentByte over = stamper.getOverContent(1);

        // ============================================================
        // 📍 COORDONNÉES PAR DÉFAUT (POUR BERRECHID, SIDI KACEM, ETC.)
        // ============================================================
        float xResponsable = 280f;
        float yResponsable = 602f;

        float xEtablissement = 280f;
        float yEtablissement = 575f;

        float xCommune = 280f;
        float yCommune = 547f;

        float xDateArrivee = 220f;
        float yDateArrivee = 355f;

        float xDateDepart = 220f;
        float yDateDepart = 327f;

        float xNbBeneficiaires = 180f;
        float yNbBeneficiaires = 300f;

        // ============================================================
        // ⚠️ COORDONNÉES SPÉCIFIQUES POUR TAOUNATE (UNIQUEMENT)
        // ============================================================
        boolean isTaounate = templatePath.toLowerCase().contains("taounate");

        if (isTaounate) {
            // Ajuste ces valeurs selon le design de ton PDF Taounate
            xResponsable = 300f;   // Déplace vers la droite
            yResponsable = 600f;   // Déplace vers le bas/haut

            xEtablissement = 300f;
            yEtablissement = 572f;

            xCommune = 300f;
            yCommune = 545f;

            xDateArrivee = 160f;   // Déplace vers la gauche
            yDateArrivee = 326f;   // Déplace vers le bas

            xDateDepart = 160f;
            yDateDepart = 298f;

            xNbBeneficiaires = 140f;
            yNbBeneficiaires = 270f;
        }


        // 1. Responsable - ligne "يشهد السيد/ة"
        over.beginText();
        over.setFontAndSize(baseFont, 14);
        over.setTextMatrix(xResponsable, yResponsable);
        over.showText(responsable);
        over.endText();

        // 2. Établissement - ligne "مدير/ة مؤسسة"
        over.beginText();
        over.setFontAndSize(baseFont, 14);
        over.setTextMatrix(xEtablissement, yEtablissement);
        over.showText(nomEtablissement);
        over.endText();

        // 3. Commune - ligne "جماعة"
        over.beginText();
        over.setFontAndSize(baseFont, 14);
        over.setTextMatrix(xCommune, yCommune);
        over.showText(commune);
        over.endText();

        // 4. Date arrivée - ligne "تاريخ الوصول"
        over.beginText();
        over.setFontAndSize(baseFont, 14);
        over.setTextMatrix(xDateArrivee, yDateArrivee);
        over.showText(dateArrivee);
        over.endText();

        // 5. Date départ - ligne "تاريخ المغادرة"
        over.beginText();
        over.setFontAndSize(baseFont, 14);
        over.setTextMatrix(xDateDepart, yDateDepart);
        over.showText(dateDepart);
        over.endText();

        // 6. Nb bénéficiaires - ligne "عدد التلميذات"
        over.beginText();
        over.setFontAndSize(baseFont, 14);
        over.setTextMatrix(xNbBeneficiaires, yNbBeneficiaires);
        over.showText(nbBeneficiaires);
        over.endText();

        stamper.close();
        reader.close();

        return baos.toByteArray();
    }

    private String getFirstCheckinDate(InterventionResponse response) {
        if (response.getCheckInOuts() == null || response.getCheckInOuts().isEmpty()) {
            return "";
        }
        return response.getCheckInOuts().stream()
                .filter(v -> v.getDateHeureCheckin() != null)
                .findFirst()
                .map(v -> v.getDateHeureCheckin().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")))
                .orElse("");
    }

    private String getLastCheckoutDate(InterventionResponse response) {
        if (response.getCheckInOuts() == null || response.getCheckInOuts().isEmpty()) {
            return "";
        }

        CheckInOutDto dernier = null;
        for (CheckInOutDto v : response.getCheckInOuts()) {
            if (v.getDateHeureCheckout() != null) {
                dernier = v;
            }
        }

        return dernier != null ?
                dernier.getDateHeureCheckout().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "";
    }

    private void sauvegarderAttestation(Integer interventionId,
                                        InterventionResponse response,
                                        Etablissement etablissement) {
        try {
            Intervention intervention = interventionService.getInterventionEntity(interventionId);
            List<Attestation> existing = attestationRepository.findByIntervention(intervention);

            Attestation attestation;
            if (!existing.isEmpty()) {
                attestation = existing.get(0);
            } else {
                attestation = new Attestation();
                attestation.setIntervention(intervention);
            }

            String responsableNom = etablissement.getResponsable() != null ?
                    etablissement.getResponsable().getPrenom() + " " + etablissement.getResponsable().getNom() : "";

            attestation.setNomSignataire(responsableNom);
            attestation.setDateSignature(LocalDateTime.now());
            attestation.setValide(false);
            attestation.setStatut("GENEREE");
            attestation.setDateGeneration(LocalDateTime.now());

            attestationRepository.save(attestation);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void uploadAttestationSignee(Integer interventionId, String cheminFichierSigne) {
        try {
            Intervention intervention = interventionService.getInterventionEntity(interventionId);
            List<Attestation> existing = attestationRepository.findByIntervention(intervention);

            Attestation attestation;
            if (!existing.isEmpty()) {
                attestation = existing.get(0);
            } else {
                attestation = new Attestation();
                attestation.setIntervention(intervention);
            }

            attestation.setCheminFichierSigne(cheminFichierSigne);
            attestation.setDateUploadSigne(LocalDateTime.now());
            attestation.setStatut("SIGNEE");

            attestationRepository.save(attestation);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}