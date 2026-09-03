package com.example.backend.dto.ia;

public class ChatIaRequest {
    private String message;
    private String role;    // ex: "ADMINISTRATEUR" -- rempli par le Controller depuis le JWT, JAMAIS par Angular
    private String prenom;  // ex: "Assia" -- idem

    public ChatIaRequest() {}

    public ChatIaRequest(String message, String role, String prenom) {
        this.message = message;
        this.role = role;
        this.prenom = prenom;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }
}