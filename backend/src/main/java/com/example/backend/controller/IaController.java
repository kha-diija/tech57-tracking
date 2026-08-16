package com.example.backend.controller;

import com.example.backend.dto.ia.ChatIaRequest;
import com.example.backend.dto.ia.ChatIaResponse;
import com.example.backend.security.UserPrincipal;
import com.example.backend.service.ia.IaService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ia")
@CrossOrigin(origins = "*")
public class IaController {

    private final IaService iaService;

    public IaController(IaService iaService) {
        this.iaService = iaService;
    }

    @PostMapping("/chat")
    public ResponseEntity<ChatIaResponse> chat(
            @RequestBody ChatIaRequest requestFromAngular,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        // On ne fait JAMAIS confiance à un rôle envoyé par le front : on le
        // remplace par celui du token JWT authentifié côté serveur
        // (impossible à falsifier depuis Angular).
        ChatIaRequest requestSecurise = new ChatIaRequest(
                requestFromAngular.getMessage(),
                principal != null ? principal.getTypeUtilisateur() : null,
                principal != null ? principal.getPrenom() : null
        );

        ChatIaResponse response = iaService.poserQuestion(requestSecurise);
        return ResponseEntity.ok(response);
    }
}