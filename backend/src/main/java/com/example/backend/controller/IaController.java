package com.example.backend.controller;

import com.example.backend.dto.ia.ChatIaRequest;
import com.example.backend.dto.ia.ChatIaResponse;
import com.example.backend.service.ia.IaService;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<ChatIaResponse> chat(@RequestBody ChatIaRequest request) {
        ChatIaResponse response = iaService.poserQuestion(request);
        return ResponseEntity.ok(response);
    }
}