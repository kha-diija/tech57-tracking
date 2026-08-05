package com.example.backend.exception;

/**
 * Exception métier levée pour signaler une erreur fonctionnelle
 * (coordonnées invalides, service de routage indisponible, etc.)
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}