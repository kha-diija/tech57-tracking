package com.example.backend.repository.admin;

import com.example.backend.dto.admin.etablissement.CommuneResponse;
import com.example.backend.entity.Commune;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommuneRepository extends JpaRepository<Commune, Integer> {
    List<Commune> findByProvince_IdProvince(Integer idProvince);
    Optional<Commune> findByNomIgnoreCaseAndProvince_IdProvince(String nom, Integer idProvince);

    // ✅ NOUVELLE REQUÊTE : Récupère la commune ET le nom de sa province en une seule fois
    @Query("SELECT new com.example.backend.dto.admin.etablissement.CommuneResponse(c.idCommune, c.nom, c.code, p.idProvince, p.nom) " +
            "FROM Commune c JOIN c.province p WHERE p.idProvince = :idProvince")
    List<CommuneResponse> findCommunesWithProvinceName(@Param("idProvince") Integer idProvince);
}