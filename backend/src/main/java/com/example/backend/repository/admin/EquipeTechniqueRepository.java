package com.example.backend.repository.admin;

import com.example.backend.entity.EquipeTechnique;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EquipeTechniqueRepository extends JpaRepository<EquipeTechnique, Integer> {

    // [NOUVEAU] Trouver l'équipe technique d'un technicien via la table de jointure equipe_membre
    @Query("SELECT e FROM EquipeTechnique e JOIN e.membres m WHERE m.id = :idTechnicien")
    Optional<EquipeTechnique> findByMembreId(@Param("idTechnicien") Integer idTechnicien);
}