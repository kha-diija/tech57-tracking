package com.example.backend.service.admin;

import com.example.backend.dto.admin.intervention.*;
import com.example.backend.entity.Rapport;
import com.example.backend.repository.admin.RapportRepository;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class RapportPdfService {

    private final InterventionService interventionService;
    private final RapportRepository rapportRepository;

    @Value("${app.base-url}")
    private String baseUrl;

    public RapportPdfService(InterventionService interventionService, RapportRepository rapportRepository) {
        this.interventionService = interventionService;
        this.rapportRepository = rapportRepository;
    }

    public byte[] genererRapportPdf(Integer interventionId) throws DocumentException, IOException {
        // 1. Récupérer les données via votre service existant
        InterventionResponse intervention = interventionService.getById(interventionId);

        // 2. Créer le document PDF
        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, baos);

        document.open();

        // 3. Titre
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, BaseColor.ORANGE);
        Paragraph title = new Paragraph("RAPPORT D'INTERVENTION", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        document.add(new Paragraph("\n"));

        Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.DARK_GRAY);
        Paragraph subtitle = new Paragraph("TECH-57 - Système de Suivi Logistique", subtitleFont);
        subtitle.setAlignment(Element.ALIGN_CENTER);
        document.add(subtitle);

        document.add(new Paragraph("Généré le : " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm")), FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.GRAY)));
        document.add(new Paragraph("\n"));
        document.add(new Paragraph("-------------------------------------------------------------------", FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.LIGHT_GRAY)));
        document.add(new Paragraph("\n"));

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        // 4. Section : Informations Générales
        addSectionHeader(document, "📋 Informations Générales");
        document.add(new Paragraph("Intervention N° : " + intervention.getId()));
        document.add(new Paragraph("Mission : " + intervention.getMissionReference()));
        document.add(new Paragraph("Établissement : " + (intervention.getEtablissementDesignation() != null ? intervention.getEtablissementDesignation() : "Non renseigné")));
        document.add(new Paragraph("Technicien : " + intervention.getTechnicienNom()));
        document.add(new Paragraph("Statut : " + intervention.getStatut()));
        document.add(new Paragraph("Date prévue : " + (intervention.getDatePrevue() != null ? intervention.getDatePrevue().format(dtf) : "Non renseignée")));
        document.add(new Paragraph("Visites effectuées : " + intervention.getNumeroVisite() + " / 2 requises"));
        document.add(new Paragraph("Taux d'avancement : " + (intervention.getTauxAvancement() != null ? intervention.getTauxAvancement() : 0) + " %"));
        document.add(new Paragraph("\n"));

        // 5. Section : Déroulement — basé sur les VRAIES visites (check-in / check-out)
        addSectionHeader(document, "⏱️ Déroulement de l'intervention (historique des visites)");

        List<CheckInOutDto> visites = intervention.getCheckInOuts();

        if (visites != null && !visites.isEmpty()) {
            PdfPTable tableVisites = new PdfPTable(4);
            tableVisites.setWidthPercentage(100);
            tableVisites.setSpacingBefore(10f);
            tableVisites.setSpacingAfter(10f);
            tableVisites.setWidths(new float[]{1.5f, 2.5f, 2.5f, 1.5f});

            addTableHeader(tableVisites, "Visite N°", "Check-in", "Check-out", "Durée");
            for (CheckInOutDto v : visites) {
                tableVisites.addCell("Visite " + v.getNumeroVisite());
                tableVisites.addCell(v.getDateHeureCheckin() != null ? v.getDateHeureCheckin().format(dtf) : "-");
                tableVisites.addCell(v.getDateHeureCheckout() != null ? v.getDateHeureCheckout().format(dtf) : "En cours...");
                tableVisites.addCell(v.getDureeMinutes() != null ? v.getDureeMinutes() + " min" : "-");
            }
            document.add(tableVisites);
        } else {
            document.add(new Paragraph("Aucune visite enregistrée pour cette intervention.", FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10, BaseColor.GRAY)));
        }

        document.add(new Paragraph("\n"));
        document.add(new Paragraph("Localisation GPS : " + (intervention.getLocalisationGps() != null ? intervention.getLocalisationGps() : "Non renseignée")));
        document.add(new Paragraph("\n"));

        // 6. Section : Matériel
        addSectionHeader(document, "📦 Suivi du Matériel");

        // 6.1 Sorties de matériel
        if (intervention.getSortiesMateriel() != null && !intervention.getSortiesMateriel().isEmpty()) {
            document.add(new Paragraph("Matériels sortis du stock :"));
            PdfPTable tableSortie = new PdfPTable(3);
            tableSortie.setWidthPercentage(100);
            tableSortie.setSpacingBefore(10f);
            tableSortie.setSpacingAfter(10f);

            addTableHeader(tableSortie, "Référence", "Quantité", "Date sortie");
            for (SortieMaterielDto s : intervention.getSortiesMateriel()) {
                tableSortie.addCell(s.getMaterielReference());
                tableSortie.addCell(String.valueOf(s.getQuantite()));
                tableSortie.addCell(s.getDateSortie() != null ? s.getDateSortie().format(DateTimeFormatter.ofPattern("dd/MM HH:mm")) : "-");
            }
            document.add(tableSortie);
        }

        // 6.2 Retours de matériel
        if (intervention.getRetoursMateriel() != null && !intervention.getRetoursMateriel().isEmpty()) {
            document.add(new Paragraph("\nMatériels retournés au stock :"));
            PdfPTable tableRetour = new PdfPTable(4);
            tableRetour.setWidthPercentage(100);
            tableRetour.setSpacingBefore(10f);
            tableRetour.setSpacingAfter(10f);

            addTableHeader(tableRetour, "Référence", "Quantité", "État", "Date retour");
            for (RetourMaterielDto r : intervention.getRetoursMateriel()) {
                tableRetour.addCell(r.getMaterielReference());
                tableRetour.addCell(String.valueOf(r.getQuantite()));
                tableRetour.addCell(r.getEtatMateriel());
                tableRetour.addCell(r.getDateRetour() != null ? r.getDateRetour().format(DateTimeFormatter.ofPattern("dd/MM HH:mm")) : "-");
            }
            document.add(tableRetour);
        }

        // 7. Section : Attestation
        if (intervention.getAttestation() != null) {
            document.add(new Paragraph("\n"));
            addSectionHeader(document, "✅ Attestation de réalisation");
            document.add(new Paragraph("Signataire : " + intervention.getAttestation().getNomSignataire()));
            document.add(new Paragraph("Date de signature : " + (intervention.getAttestation().getDateSignature() != null ? intervention.getAttestation().getDateSignature().format(dtf) : "Non signé")));
            document.add(new Paragraph("Validité : " + (intervention.getAttestation().getValide() ? "Validée" : "En attente")));

            // ✅ AJOUT : Insérer l'image de la signature
            String signatureBase64 = intervention.getAttestation().getSignatureNumerique();
            if (signatureBase64 != null && !signatureBase64.isEmpty()) {
                try {
                    String base64Data = signatureBase64;
                    if (base64Data.contains(",")) {
                        base64Data = base64Data.substring(base64Data.indexOf(",") + 1);
                    }

                    byte[] imageBytes = java.util.Base64.getDecoder().decode(base64Data);

                    Image signatureImage = Image.getInstance(imageBytes);
                    signatureImage.scaleToFit(150, 80);
                    signatureImage.setAlignment(Element.ALIGN_RIGHT);

                    document.add(new Paragraph("\n"));
                    document.add(signatureImage);
                } catch (Exception e) {
                    document.add(new Paragraph("(Signature non disponible)", FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.RED)));
                }
            }
        }

        // 8. Section : Photos (Avant / Après) - AVEC AFFICHAGE DES IMAGES
        if (intervention.getPhotos() != null && !intervention.getPhotos().isEmpty()) {
            document.add(new Paragraph("\n"));
            addSectionHeader(document, "📸 Photos de l'intervention");

            for (PhotoDto p : intervention.getPhotos()) {
                document.add(new Paragraph(p.getTypePhoto() + " :", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.DARK_GRAY)));
                try {
                    // Construire l'URL complète à partir du chemin
                    String chemin = p.getCheminFichier();
                    String urlComplete;

                    // Si c'est déjà une URL complète (http/https), on la garde telle quelle
                    if (chemin != null && chemin.startsWith("http")) {
                        urlComplete = chemin;
                    } else {
                        // ✅ CORRECTION : Encoder les espaces et caractères spéciaux
                        String encodedChemin = chemin.replace(" ", "%20");
                        urlComplete = baseUrl + encodedChemin;
                    }

                    // Télécharger l'image depuis l'URL complète
                    Image image = Image.getInstance(new URL(urlComplete));

                    // ✅ Redimensionner sans déformer (préserve les proportions)
                    image.scaleToFit(400, 400);
                    image.setAlignment(Element.ALIGN_CENTER);
                    document.add(image);
                } catch (Exception e) {
                    document.add(new Paragraph("(Impossible de charger l'image)", FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.RED)));
                }
                document.add(new Paragraph("\n"));
            }
        }

        // 9. Pied de page
        document.add(new Paragraph("\n"));
        document.add(new Paragraph("-------------------------------------------------------------------", FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.LIGHT_GRAY)));
        document.add(new Paragraph("Document généré automatiquement par le système TECH-57.", FontFactory.getFont(FontFactory.HELVETICA, 8, BaseColor.GRAY)));

        document.close();
        byte[] pdfBytes = baos.toByteArray();

        // ==============================================================
        // --- Enregistrer le rapport dans la base de données ---
        // ==============================================================
        Rapport rapport = new Rapport();
        rapport.setTitre("Rapport Intervention #" + interventionId);
        rapport.setFormat("PDF");
        rapport.setDateGeneration(LocalDateTime.now());
        rapport.setGenereParIa(false);
        rapport.setIntervention(interventionService.getInterventionEntity(interventionId));

        rapportRepository.save(rapport);

        return pdfBytes;
    }

    // ==============================================================
    // --- Génération de l'attestation en PDF ---
    // ==============================================================
    public byte[] genererAttestationPdf(Integer interventionId) throws DocumentException, IOException {
        InterventionResponse intervention = interventionService.getById(interventionId);

        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, baos);
        document.open();

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, BaseColor.ORANGE);
        Paragraph title = new Paragraph("ATTESTATION DE RÉALISATION", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        document.add(new Paragraph("\n"));

        Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.DARK_GRAY);
        Paragraph subtitle = new Paragraph("TECH-57 - Système de Suivi Logistique", subtitleFont);
        subtitle.setAlignment(Element.ALIGN_CENTER);
        document.add(subtitle);
        document.add(new Paragraph("\n"));
        document.add(new Paragraph("-------------------------------------------------------------------", FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.LIGHT_GRAY)));
        document.add(new Paragraph("\n"));

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        document.add(new Paragraph("Intervention N° : " + intervention.getId()));
        document.add(new Paragraph("Mission : " + intervention.getMissionReference()));
        document.add(new Paragraph("Établissement : " + (intervention.getEtablissementDesignation() != null ? intervention.getEtablissementDesignation() : "Non renseigné")));
        document.add(new Paragraph("Technicien : " + intervention.getTechnicienNom()));
        document.add(new Paragraph("\n"));

        if (intervention.getAttestation() != null) {
            document.add(new Paragraph("Signataire : " + intervention.getAttestation().getNomSignataire()));
            document.add(new Paragraph("Date de signature : " +
                    (intervention.getAttestation().getDateSignature() != null
                            ? intervention.getAttestation().getDateSignature().format(dtf)
                            : "Non signé")));
            document.add(new Paragraph("Validité : " + (intervention.getAttestation().getValide() ? "Validée" : "En attente")));

            // ✅ AJOUT : Insérer l'image de la signature
            String signatureBase64 = intervention.getAttestation().getSignatureNumerique();
            if (signatureBase64 != null && !signatureBase64.isEmpty()) {
                try {
                    String base64Data = signatureBase64;
                    if (base64Data.contains(",")) {
                        base64Data = base64Data.substring(base64Data.indexOf(",") + 1);
                    }

                    byte[] imageBytes = java.util.Base64.getDecoder().decode(base64Data);

                    Image signatureImage = Image.getInstance(imageBytes);
                    signatureImage.scaleToFit(150, 80);
                    signatureImage.setAlignment(Element.ALIGN_RIGHT);

                    document.add(new Paragraph("\n"));
                    document.add(signatureImage);
                } catch (Exception e) {
                    document.add(new Paragraph("(Signature non disponible)", FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.RED)));
                }
            }
        } else {
            document.add(new Paragraph("Aucune attestation enregistrée pour cette intervention."));
        }

        document.add(new Paragraph("\n"));
        document.add(new Paragraph("Ce document atteste de la bonne réalisation de l'intervention."));
        document.add(new Paragraph("Généré le : " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm")), FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.GRAY)));

        document.close();
        return baos.toByteArray();
    }

    // Méthodes utilitaires pour le style
    private void addSectionHeader(Document document, String text) throws DocumentException {
        Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, BaseColor.DARK_GRAY);
        Paragraph p = new Paragraph(text, font);
        p.setSpacingBefore(15f);
        p.setSpacingAfter(5f);
        document.add(p);
    }

    private void addTableHeader(PdfPTable table, String... headers) {
        Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.WHITE);
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, font));
            cell.setBackgroundColor(BaseColor.DARK_GRAY);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(5);
            table.addCell(cell);
        }
    }
}