package com.example.backend.dto.admin.Mission;

import com.example.backend.entity.MissionInstallation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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

    // Infos de l'établissement
    private Integer idEtablissement;
    private String etablissementDesignation;
    private String etablissementReference;

    // Infos de l'administrateur
    private Integer idAdministrateur;
    private String adminNomComplet;

    // Infos de l'équipe technique (optionnel)
    private Integer idEquipe;
    private String equipeNom;

    // Constructeur de conversion depuis l'entité
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
            // getId() vient de la classe parente Utilisateur
            this.idAdministrateur = m.getAdministrateur().getId();

            // getPrenom() et getNom() viennent aussi de Utilisateur
            String prenom = m.getAdministrateur().getPrenom() != null ? m.getAdministrateur().getPrenom() : "";
            String nom = m.getAdministrateur().getNom() != null ? m.getAdministrateur().getNom() : "";
            this.adminNomComplet = (prenom + " " + nom).trim();
        }

        if (m.getEquipe() != null) {
            this.idEquipe = m.getEquipe().getIdEquipe();
            this.equipeNom = m.getEquipe().getNomEquipe();
        }
    }
}