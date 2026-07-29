package com.example.backend.service;

import com.example.backend.dto.MaintenanceDTO;
import com.example.backend.dto.MaintenanceRequest;

import java.util.List;

public interface MaintenanceService {

    List<MaintenanceDTO> getHistoriqueByMateriel(Integer idMateriel);

    MaintenanceDTO getById(Integer id);

    MaintenanceDTO creer(MaintenanceRequest request);

    MaintenanceDTO modifier(Integer id, MaintenanceRequest request);

    void supprimer(Integer id);

    /** Clôture la maintenance en cours et repasse le matériel disponible. */
    MaintenanceDTO cloturer(Integer id);
}