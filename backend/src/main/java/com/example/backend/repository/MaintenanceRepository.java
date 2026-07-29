package com.example.backend.repository;

import com.example.backend.entity.Maintenance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MaintenanceRepository extends JpaRepository<Maintenance, Integer> {

    List<Maintenance> findByMateriel_IdMaterielOrderByDateMaintenanceDesc(Integer idMateriel);

    // Une maintenance en cours (disponible = false) pour un matériel donné
    boolean existsByMateriel_IdMaterielAndDisponibleFalse(Integer idMateriel);
}