package com.example.backend.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

/**
 * Sous-classe `administrateur` (table-per-subclass, PK = FK vers utilisateur).
 */
@Entity
@Table(name = "administrateur")
@DiscriminatorValue("ADMINISTRATEUR")
@PrimaryKeyJoinColumn(name = "id_utilisateur")
public class Administrateur extends Utilisateur {
}