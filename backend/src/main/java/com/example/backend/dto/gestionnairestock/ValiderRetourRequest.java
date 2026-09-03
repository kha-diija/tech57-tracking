package com.example.backend.dto.gestionnairestock;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class ValiderRetourRequest {

    @NotEmpty(message = "Au moins une ligne de retour est requise.")
    @Valid
    private List<LigneRetourRequest> lignes;

    @Data
    public static class LigneRetourRequest {
        @NotNull
        private Integer idMateriel;

        @NotNull
        @Min(0)
        private Integer quantiteBonEtat;

        @NotNull
        @Min(0)
        private Integer quantiteEnPanne;
    }
}