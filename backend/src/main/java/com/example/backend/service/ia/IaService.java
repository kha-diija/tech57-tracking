package com.example.backend.service.ia;

import com.example.backend.dto.ia.ChatIaRequest;
import com.example.backend.dto.ia.ChatIaResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Service
public class IaService {

    private final RestClient restClient;

    public IaService(@Value("${ia.service.url:http://localhost:8000}") String iaServiceUrl) {
        // Configuration des timeouts de connexion et de lecture
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout((int) Duration.ofSeconds(10).toMillis()); // 10 secondes pour établir la connexion
        factory.setReadTimeout((int) Duration.ofSeconds(60).toMillis());   // 60 secondes pour lire la réponse de l'IA

        this.restClient = RestClient.builder()
                .baseUrl(iaServiceUrl)
                .requestFactory(factory)
                .build();
    }

    public ChatIaResponse poserQuestion(ChatIaRequest request) {
        return restClient.post()
                .uri("/api/ia/chat")
                .body(request)
                .retrieve()
                .body(ChatIaResponse.class);
    }
}