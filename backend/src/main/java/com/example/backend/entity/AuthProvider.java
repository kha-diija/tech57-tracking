package com.example.backend.entity;

/**
 * Correspond à la contrainte CHECK (auth_provider IN ('LOCAL','GOOGLE'))
 * de la table utilisateur.
 */
public enum AuthProvider {
    LOCAL,
    GOOGLE
}
