package com.example.backend.service;

import com.example.backend.dto.NotificationDto;
import com.example.backend.entity.Administrateur;
import com.example.backend.entity.Notification;
import com.example.backend.entity.Utilisateur;
import com.example.backend.repository.AdministrateurRepository;
import com.example.backend.repository.NotificationRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationHelperService {

    private final NotificationRepository notificationRepository;
    private final AdministrateurRepository administrateurRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public NotificationHelperService(NotificationRepository notificationRepository,
                                     AdministrateurRepository administrateurRepository,
                                     SimpMessagingTemplate messagingTemplate) {
        this.notificationRepository = notificationRepository;
        this.administrateurRepository = administrateurRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void notifierTousLesAdmins(Utilisateur expediteur, String message, String type) {
        List<Administrateur> admins = administrateurRepository.findAll();
        for (Administrateur admin : admins) {
            envoyerNotification(expediteur, admin, message, type);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void envoyerNotification(Utilisateur expediteur, Utilisateur destinataire, String message, String type) {
        Notification notif = new Notification();
        notif.setExpediteur(expediteur);
        notif.setDestinataire(destinataire);
        notif.setMessage(message);
        notif.setType(type);
        Notification saved = notificationRepository.save(notif);

        NotificationDto dto = toDto(saved);
        messagingTemplate.convertAndSendToUser(destinataire.getEmail(), "/queue/notifications", dto);
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