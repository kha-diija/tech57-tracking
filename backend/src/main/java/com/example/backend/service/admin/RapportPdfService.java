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
        InterventionResponse intervention = interventionService.getById(interventionId);

        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, baos);

        document.open();

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

        addSectionHeader(document, "📦 Suivi du Matériel");

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

        // Attestation (informative uniquement)
        if (intervention.getAttestation() != null) {
            document.add(new Paragraph("\n"));
            addSectionHeader(document, "✅ Attestation de réalisation");
            document.add(new Paragraph("Signataire : " + intervention.getAttestation().getNomSignataire()));
            document.add(new Paragraph("Date de signature : " + (intervention.getAttestation().getDateSignature() != null ? intervention.getAttestation().getDateSignature().format(dtf) : "Non signé")));
            document.add(new Paragraph("Validité : " + (intervention.getAttestation().getValide() ? "Validée" : "En attente")));
        }

        if (intervention.getPhotos() != null && !intervention.getPhotos().isEmpty()) {
            document.add(new Paragraph("\n"));
            addSectionHeader(document, "📸 Photos de l'intervention");

            for (PhotoDto p : intervention.getPhotos()) {
                document.add(new Paragraph(p.getTypePhoto() + " :", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.DARK_GRAY)));
                try {
                    String chemin = p.getCheminFichier();
                    String urlComplete;

                    if (chemin != null && chemin.startsWith("http")) {
                        urlComplete = chemin;
                    } else {
                        String encodedChemin = chemin.replace(" ", "%20");
                        urlComplete = baseUrl + encodedChemin;
                    }

                    Image image = Image.getInstance(new URL(urlComplete));
                    image.scaleToFit(400, 400);
                    image.setAlignment(Element.ALIGN_CENTER);
                    document.add(image);
                } catch (Exception e) {
                    document.add(new Paragraph("(Impossible de charger l'image)", FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.RED)));
                }
                document.add(new Paragraph("\n"));
            }
        }

        document.add(new Paragraph("\n"));
        document.add(new Paragraph("-------------------------------------------------------------------", FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.LIGHT_GRAY)));
        document.add(new Paragraph("Document généré automatiquement par le système TECH-57.", FontFactory.getFont(FontFactory.HELVETICA, 8, BaseColor.GRAY)));

        document.close();
        byte[] pdfBytes = baos.toByteArray();

        Rapport rapport = new Rapport();
        rapport.setTitre("Rapport Intervention #" + interventionId);
        rapport.setFormat("PDF");
        rapport.setDateGeneration(LocalDateTime.now());
        rapport.setGenereParIa(false);
        rapport.setIntervention(interventionService.getInterventionEntity(interventionId));

        rapportRepository.save(rapport);

        return pdfBytes;
    }

    public byte[] genererAttestationPdf(Integer interventionId) throws DocumentException, IOException {
        InterventionResponse intervention = interventionService.getById(interventionId);

        String nomSignataire = intervention.getAttestation() != null
                ? intervention.getAttestation().getNomSignataire()
                : null;

        return construireAttestationPdf(
                intervention,
                nomSignataire,
                intervention.getSortiesMateriel(),
                intervention.getRetoursMateriel(),
                intervention.getChecklistItems()
        );
    }

    public byte[] genererAttestationPreviewPdf(InterventionResponse intervention,
                                               String nomSignataire,
                                               List<SortieMaterielDto> sortiesPreview,
                                               List<RetourMaterielDto> retoursPreview,
                                               List<ChecklistItemDto> checklistPreview) throws DocumentException, IOException {
        return construireAttestationPdf(intervention, nomSignataire, sortiesPreview, retoursPreview, checklistPreview);
    }

    /**
     * Construction commune du PDF d'attestation.
     * Le tableau de la checklist est réduit à 2 colonnes : Matériel et État constaté (vide).
     */
    private byte[] construireAttestationPdf(InterventionResponse intervention,
                                            String nomSignataire,
                                            List<SortieMaterielDto> sorties,
                                            List<RetourMaterielDto> retours,
                                            List<ChecklistItemDto> checklist) throws DocumentException, IOException {
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

        addSectionHeader(document, "📋 Informations Générales");
        document.add(new Paragraph("Intervention N° : " + intervention.getId()));
        document.add(new Paragraph("Mission : " + intervention.getMissionReference()));
        document.add(new Paragraph("Établissement : " + (intervention.getEtablissementDesignation() != null ? intervention.getEtablissementDesignation() : "Non renseigné")));
        document.add(new Paragraph("Technicien : " + intervention.getTechnicienNom()));
        document.add(new Paragraph("Visites effectuées : " + intervention.getNumeroVisite()));
        document.add(new Paragraph("\n"));

        if (sorties != null && !sorties.isEmpty()) {
            addSectionHeader(document, "📤 Matériel sorti / utilisé");
            PdfPTable table = new PdfPTable(3);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);
            table.setSpacingAfter(10f);
            addTableHeader(table, "Référence", "Quantité", "Date");
            for (SortieMaterielDto s : sorties) {
                table.addCell(s.getMaterielReference());
                table.addCell(String.valueOf(s.getQuantite()));
                table.addCell(s.getDateSortie() != null ? s.getDateSortie().format(DateTimeFormatter.ofPattern("dd/MM HH:mm")) : "-");
            }
            document.add(table);
        }

        if (retours != null && !retours.isEmpty()) {
            addSectionHeader(document, "📥 Matériel retourné");
            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);
            table.setSpacingAfter(10f);
            addTableHeader(table, "Référence", "Quantité", "État", "Date");
            for (RetourMaterielDto r : retours) {
                table.addCell(r.getMaterielReference());
                table.addCell(String.valueOf(r.getQuantite()));
                table.addCell(r.getEtatMateriel());
                table.addCell(r.getDateRetour() != null ? r.getDateRetour().format(DateTimeFormatter.ofPattern("dd/MM HH:mm")) : "-");
            }
            document.add(table);
        }

        // --- Checklist : tableau 2 colonnes (Matériel, État constaté) avec état constaté vide ---
        if (checklist != null && !checklist.isEmpty()) {
            addSectionHeader(document, "📋 Checklist d'installation");
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);
            table.setSpacingAfter(10f);
            addTableHeader(table, "Matériel", "État constaté");
            for (ChecklistItemDto c : checklist) {
                String materiel = c.getMaterielReference() != null ? c.getMaterielReference() : "-";
                table.addCell(materiel);
                table.addCell(""); // cellule vide
            }
            document.add(table);
        }

        document.add(new Paragraph("\n"));
        document.add(new Paragraph("Ce document atteste de la bonne réalisation de l'intervention."));
        document.add(new Paragraph("Généré le : " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm")), FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.GRAY)));

        // Nom du signataire en bas à droite
        document.add(new Paragraph("\n\n"));
        Font signFont = FontFactory.getFont(FontFactory.HELVETICA, 11, BaseColor.BLACK);

        Paragraph signLine = new Paragraph(
                (nomSignataire != null && !nomSignataire.isBlank())
                        ? "Signataire : " + nomSignataire
                        : "Signataire : ____________________",
                signFont
        );
        signLine.setAlignment(Element.ALIGN_RIGHT);
        document.add(signLine);

        Paragraph signSpace = new Paragraph("Signature : ______________________", signFont);
        signSpace.setAlignment(Element.ALIGN_RIGHT);
        signSpace.setSpacingBefore(30f);
        document.add(signSpace);

        document.close();
        return baos.toByteArray();
    }

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