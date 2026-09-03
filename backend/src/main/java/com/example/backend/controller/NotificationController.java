package com.example.backend.controller;

import com.example.backend.dto.NotificationDto;
import com.example.backend.security.UserPrincipal;
import com.example.backend.service.NotificationService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public List<NotificationDto> lister(@AuthenticationPrincipal UserPrincipal principal) {
        return notificationService.listerPourUtilisateur(principal.getId());
    }

    @GetMapping("/non-lues/count")
    public Map<String, Long> compterNonLues(@AuthenticationPrincipal UserPrincipal principal) {
        return Map.of("count", notificationService.compterNonLues(principal.getId()));
    }

    @PatchMapping("/{id}/lu")
    public void marquerCommeLue(@PathVariable Integer id, @AuthenticationPrincipal UserPrincipal principal) {
        notificationService.marquerCommeLue(id, principal.getId());
    }

    @PatchMapping("/lu-toutes")
    public void marquerToutesCommeLues(@AuthenticationPrincipal UserPrincipal principal) {
        notificationService.marquerToutesCommeLues(principal.getId());
    }
}