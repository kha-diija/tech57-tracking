package com.example.backend.dto;

import lombok.*;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MaterielDTO {

    private Integer idMateriel;
    private String reference;
    private String nom;
    private String numeroSerie;
    private String codeQr;
    private String etat; // Neuf / En service / En panne / Retiré

    private Integer idCategorie;
    private String nomCategorie;
    private Boolean estKit;

    private Integer idEtablissement;
    private String designationEtablissement;

    private Integer idMaterielParent;
    private Integer quantiteComposant;

    // Arborescence : composants directs (peuplé uniquement pour un kit, sur la fiche détail)
    private List<MaterielDTO> composants;

    // Indicateur pratique pour le front (badge "en maintenance")
    private Boolean enMaintenance;
}