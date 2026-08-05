package com.example.backend.service;

import com.example.backend.dto.MaintenanceDTO;
import com.example.backend.dto.MaintenanceRequest;
import com.example.backend.entity.Maintenance;
import com.example.backend.entity.Materiel;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.repository.MaintenanceRepository;
import com.example.backend.repository.adminMaterielRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class MaintenanceServiceImpl implements MaintenanceService {

    private final MaintenanceRepository maintenanceRepository;
    private final adminMaterielRepository adminMaterielRepository;

    @Override
    @Transactional(readOnly = true)
    public List<MaintenanceDTO> getHistoriqueByMateriel(Integer idMateriel) {
        return maintenanceRepository.findByMateriel_IdMaterielOrderByDateMaintenanceDesc(idMateriel)
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public MaintenanceDTO getById(Integer id) {
        return toDto(findOrThrow(id));
    }

    @Override
    public MaintenanceDTO creer(MaintenanceRequest request) {
        Materiel materiel = adminMaterielRepository.findById(request.getIdMateriel())
                .orElseThrow(() -> new ResourceNotFoundException("Matériel introuvable, id=" + request.getIdMateriel()));

        Maintenance maintenance = new Maintenance();
        maintenance.setDateMaintenance(request.getDateMaintenance());
        maintenance.setDescription(request.getDescription());
        maintenance.setCout(request.getCout());
        maintenance.setDisponible(Boolean.TRUE.equals(request.getDisponible()));
        maintenance.setMateriel(materiel);

        Maintenance saved = maintenanceRepository.save(maintenance);

        // Ouvrir une maintenance "indisponible" bascule automatiquement l'état du matériel
        if (!Boolean.TRUE.equals(request.getDisponible())) {
            materiel.setEtat("En panne");
            adminMaterielRepository.save(materiel);
        }

        return toDto(saved);
    }

    @Override
    public MaintenanceDTO modifier(Integer id, MaintenanceRequest request) {
        Maintenance maintenance = findOrThrow(id);
        maintenance.setDateMaintenance(request.getDateMaintenance());
        maintenance.setDescription(request.getDescription());
        maintenance.setCout(request.getCout());
        maintenance.setDisponible(Boolean.TRUE.equals(request.getDisponible()));
        return toDto(maintenanceRepository.save(maintenance));
    }

    @Override
    public void supprimer(Integer id) {
        maintenanceRepository.delete(findOrThrow(id));
    }

    @Override
    public MaintenanceDTO cloturer(Integer id) {
        Maintenance maintenance = findOrThrow(id);
        maintenance.setDisponible(true);
        maintenanceRepository.save(maintenance);

        Materiel materiel = maintenance.getMateriel();
        materiel.setEtat("En service");
        adminMaterielRepository.save(materiel);

        return toDto(maintenance);
    }

    private Maintenance findOrThrow(Integer id) {
        return maintenanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Maintenance introuvable, id=" + id));
    }

    private MaintenanceDTO toDto(Maintenance m) {
        return MaintenanceDTO.builder()
                .idMaintenance(m.getIdMaintenance())
                .dateMaintenance(m.getDateMaintenance())
                .description(m.getDescription())
                .cout(m.getCout())
                .disponible(m.getDisponible())
                .idMateriel(m.getMateriel().getIdMateriel())
                .referenceMateriel(m.getMateriel().getReference())
                .build();
    }
}