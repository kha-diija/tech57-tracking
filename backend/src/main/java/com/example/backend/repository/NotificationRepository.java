package com.example.backend.repository;

import com.example.backend.entity.Notification;
import com.example.backend.entity.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Integer> {
    List<Notification> findByDestinataireOrderByDateEnvoiDesc(Utilisateur destinataire);
}