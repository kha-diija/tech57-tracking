package com.example.backend.controller.gestionnairestock;

import com.example.backend.dto.gestionnairestock.SortieARegulariserDto;
import com.example.backend.dto.gestionnairestock.ValiderRetourRequest;
import com.example.backend.security.UserPrincipal;
import com.example.backend.service.gestionnairestock.RetourMaterielGestionService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/gestionnaire-stock/retours")
@PreAuthorize("hasAnyRole('ADMINISTRATEUR','GESTIONNAIRE_STOCK')")
public class RetourMaterielGestionController {

    private final RetourMaterielGestionService service;

    public RetourMaterielGestionController(RetourMaterielGestionService service) {
        this.service = service;
    }

    @GetMapping("/a-regulariser")
    public List<SortieARegulariserDto> listerARegulariser() {
        return service.listerARegulariser();
    }

    @PostMapping("/{idSortie}/valider")
    public void validerRetour(@PathVariable Integer idSortie,
                              @Valid @RequestBody ValiderRetourRequest request,
                              @AuthenticationPrincipal UserPrincipal principal) {
        service.validerRetour(idSortie, principal.getId(), request);
    }
}