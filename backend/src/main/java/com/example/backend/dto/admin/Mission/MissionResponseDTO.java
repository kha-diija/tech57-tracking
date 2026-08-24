package com.example.backend.dto.admin.Mission;

import com.example.backend.entity.MissionInstallation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MissionResponseDTO {
    private Integer idMission;
    private String reference;
    private String titre;
    private String statut;
    private LocalDateTime dateCreation;
    private Double budgetPropose;

    private Integer idEtablissement;
    private String etablissementDesignation;
    private String etablissementReference;

    private Integer idAdministrateur;
    private String adminNomComplet;

    private Integer idEquipe;
    private String equipeNom;

    // ✅ NOUVEAU : Liste des matériels
    private List<MissionMaterielDTO> materiels;

    public MissionResponseDTO(MissionInstallation m) {
        this.idMission = m.getIdMission();
        this.reference = m.getReference();
        this.titre = m.getTitre();
        this.statut = m.getStatut();
        this.dateCreation = m.getDateCreation();
        this.budgetPropose = m.getBudgetPropose();

        if (m.getEtablissement() != null) {
            this.idEtablissement = m.getEtablissement().getIdEtablissement();
            this.etablissementDesignation = m.getEtablissement().getDesignation();
            this.etablissementReference = m.getEtablissement().getReference();
        }

        if (m.getAdministrateur() != null) {
            this.idAdministrateur = m.getAdministrateur().getId();
            String prenom = m.getAdministrateur().getPrenom() != null ? m.getAdministrateur().getPrenom() : "";
            String nom = m.getAdministrateur().getNom() != null ? m.getAdministrateur().getNom() : "";
            this.adminNomComplet = (prenom + " " + nom).trim();
        }

        if (m.getEquipe() != null) {
            this.idEquipe = m.getEquipe().getIdEquipe();
            this.equipeNom = m.getEquipe().getNomEquipe();
        }

        // ✅ NOUVEAU : Conversion des matériels
        if (m.getMateriels() != null && !m.getMateriels().isEmpty()) {
            this.materiels = m.getMateriels().stream().map(materiel -> {
                MissionMaterielDTO dto = new MissionMaterielDTO();
                dto.setIdMateriel(materiel.getMateriel().getIdMateriel());
                dto.setQuantite(materiel.getQuantite());
                dto.setStatut(materiel.getStatut());
                dto.setMotifRejet(materiel.getMotifRejet());
                return dto;
            }).collect(Collectors.toList());
        }
    }
}