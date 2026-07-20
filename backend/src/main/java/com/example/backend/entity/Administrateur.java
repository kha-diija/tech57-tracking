package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "administrateur")
@PrimaryKeyJoinColumn(name = "id_utilisateur")
@Getter @Setter @NoArgsConstructor
public class Administrateur extends Utilisateur {
}