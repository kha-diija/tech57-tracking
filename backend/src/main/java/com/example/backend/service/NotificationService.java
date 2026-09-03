package com.example.backend.service;

import com.example.backend.dto.NotificationDto;
import com.example.backend.entity.Notification;
import com.example.backend.entity.Utilisateur;
import com.example.backend.repository.NotificationRepository;
import com.example.backend.repository.UtilisateurRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UtilisateurRepository utilisateurRepository;

    public NotificationService(NotificationRepository notificationRepository,
                               UtilisateurRepository utilisateurRepository) {
        this.notificationRepository = notificationRepository;
        this.utilisateurRepository = utilisateurRepository;
    }

    public List<NotificationDto> listerPourUtilisateur(Integer idUtilisateur) {
        Utilisateur destinataire = utilisateurRepository.findById(idUtilisateur)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur introuvable."));
        return notificationRepository.findByDestinataireOrderByDateEnvoiDesc(destinataire)
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    public long compterNonLues(Integer idUtilisateur) {
        Utilisateur destinataire = utilisateurRepository.findById(idUtilisateur)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur introuvable."));
        return notificationRepository.countByDestinataireAndLuFalse(destinataire);
    }

    @Transactional
    public void marquerCommeLue(Integer idNotification, Integer idUtilisateur) {
        Notification notif = notificationRepository.findById(idNotification)
                .orElseThrow(() -> new EntityNotFoundException("Notification introuvable."));
        if (!notif.getDestinataire().getId().equals(idUtilisateur)) {
            throw new IllegalStateException("Cette notification ne vous appartient pas.");
        }
        notif.setLu(true);
        notificationRepository.save(notif);
    }

    @Transactional
    public void marquerToutesCommeLues(Integer idUtilisateur) {
        Utilisateur destinataire = utilisateurRepository.findById(idUtilisateur)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur introuvable."));
        List<Notification> notifs = notificationRepository.findByDestinataireOrderByDateEnvoiDesc(destinataire);
        notifs.forEach(n -> n.setLu(true));
        notificationRepository.saveAll(notifs);
    }

    private NotificationDto toDto(Notification n) {
        return NotificationDto.builder()
                .idNotification(n.getIdNotification())
                .message(n.getMessage())
                .dateEnvoi(n.getDateEnvoi())
                .lu(n.getLu())
                .type(n.getType())
                .expediteurNom(n.getExpediteur() != null
                        ? n.getExpediteur().getNom() + " " + n.getExpediteur().getPrenom()
                        : null)
                .build();
    }
}