package com.example.backend.repository;

import com.example.backend.entity.Intervention;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InterventionRepositoryy extends JpaRepository<Intervention, Integer> {

    @Query("select avg(i.tauxAvancement) from Intervention i " +
            "where i.mission.etablissement.idEtablissement = :idEtablissement")
    Double findAvgTauxAvancementByEtablissement(@Param("idEtablissement") Integer idEtablissement);

    @Query("select avg(i.tauxAvancement) from Intervention i " +
            "where i.mission.etablissement.commune.province.idProvince = :idProvince")
    Double findAvgTauxAvancementByProvince(@Param("idProvince") Integer idProvince);
}