package com.example.backend.dto.ia;

public class ChatIaResponse {
    private String response;

    public ChatIaResponse() {}

    public ChatIaResponse(String response) {
        this.response = response;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }
}