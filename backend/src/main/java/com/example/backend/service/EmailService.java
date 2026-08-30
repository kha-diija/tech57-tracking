package com.example.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromAddress;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendPasswordResetEmail(String toEmail, String prenom, String rawToken) {
        String resetLink = frontendUrl + "/reset-password?token=" + rawToken;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(toEmail);
        message.setSubject("TECH-57 · Réinitialisation de votre mot de passe");
        message.setText(
                "Bonjour " + prenom + ",\n\n" +
                        "Vous avez demandé la réinitialisation de votre mot de passe.\n" +
                        "Cliquez sur le lien ci-dessous pour choisir un nouveau mot de passe (valable 30 minutes) :\n\n" +
                        resetLink + "\n\n" +
                        "Si vous n'êtes pas à l'origine de cette demande, ignorez cet email.\n\n" +
                        "L'équipe TECH-57"
        );

        mailSender.send(message);
    }
}