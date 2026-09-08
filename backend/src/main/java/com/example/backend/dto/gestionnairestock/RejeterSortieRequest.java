package com.example.backend.dto.gestionnairestock;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RejeterSortieRequest {

    @NotBlank(message = "Le motif de rejet est obligatoire.")
    private String motifRejet;
}