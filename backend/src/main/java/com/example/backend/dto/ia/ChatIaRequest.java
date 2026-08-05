package com.example.backend.dto.ia;

public class ChatIaRequest {
    private String message;

    public ChatIaRequest() {}

    public ChatIaRequest(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}