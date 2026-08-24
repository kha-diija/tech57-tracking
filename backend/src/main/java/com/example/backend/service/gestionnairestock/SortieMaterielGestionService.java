package com.example.backend.service.gestionnairestock;

import com.example.backend.dto.gestionnairestock.*;
import com.example.backend.entity.*;
import com.example.backend.repository.UtilisateurRepository;
import com.example.backend.repository.admin.MissionMaterielRepository;
import com.example.backend.repository.admin.SortieMaterielRepository;
import com.example.backend.repository.gestionnairestock.GsStockMaterielRepository;
import com.example.backend.service.NotificationHelperService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SortieMaterielGestionService {

    private final SortieMaterielRepository sortieMaterielRepository;
    private final GsStockMaterielRepository stockMaterielRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final MissionMaterielRepository missionMaterielRepository;
    private final NotificationHelperService notificationHelperService;

    public SortieMaterielGestionService(
            SortieMaterielRepository sortieMaterielRepository,
            GsStockMaterielRepository stockMaterielRepository,
            UtilisateurRepository utilisateurRepository,
            MissionMaterielRepository missionMaterielRepository,
            NotificationHelperService notificationHelperService) {
        this.sortieMaterielRepository = sortieMaterielRepository;
        this.stockMaterielRepository = stockMaterielRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.missionMaterielRepository = missionMaterielRepository;
        this.notificationHelperService = notificationHelperService;
    }

    public List<SortieMaterielDto> listerParStatut(String statut) {
        return sortieMaterielRepository.findByStatut(statut)
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    private SortieMaterielDto toDto(SortieMateriel s) {
        SortieMaterielDto dto = new SortieMaterielDto();
        dto.setIdSortie(s.getIdSortie());
        dto.setDateSortie(s.getDateSortie());
        dto.setLieuIntervention(s.getLieuIntervention());
        dto.setStatut(s.getStatut());
        dto.setMotifRejet(s.getMotif());

        if (s.getTechnicien() != null) {
            dto.setTechnicienId(s.getTechnicien().getId());
            dto.setTechnicienNom(s.getTechnicien().getNom() + " " + s.getTechnicien().getPrenom());
        }

        if (s.getMission() != null) {
            dto.setMissionReference(s.getMission().getReference());
        } else if (s.getIntervention() != null && s.getIntervention().getMission() != null) {
            dto.setMissionReference(s.getIntervention().getMission().getReference());
        }

        List<SortieMaterielDetailDto> detailDtos = s.getDetails().stream().map(d -> {
            SortieMaterielDetailDto dd = new SortieMaterielDetailDto();
            dd.setIdDetail(d.getIdDetailSortie());
            dd.setIdMateriel(d.getMateriel().getIdMateriel());
            dd.setMaterielReference(d.getMateriel().getReference());
            dd.setMaterielNom(d.getMateriel().getNom());
            dd.setQuantiteDemandee(d.getQuantite());

            stockMaterielRepository.findById(d.getMateriel().getIdMateriel())
                    .ifPresent(stock -> dd.setStockDisponible(stock.getQuantiteDisponible()));

            return dd;
        }).collect(Collectors.toList());

        dto.setDetails(detailDtos);
        return dto;
    }

    @Transactional
    public SortieMaterielDto approuver(Integer idSortie, Integer idValidateur) {
        SortieMateriel sortie = sortieMaterielRepository.findById(idSortie)
                .orElseThrow(() -> new EntityNotFoundException("Sortie introuvable."));

        if (!"En attente".equals(sortie.getStatut())) {
            throw new IllegalStateException("Cette demande a déjà été traitée.");
        }

        Utilisateur validateur = utilisateurRepository.findById(idValidateur)
                .orElseThrow(() -> new EntityNotFoundException("Validateur introuvable."));

        for (DetailSortieMateriel detail : sortie.getDetails()) {
            StockMateriel stock = stockMaterielRepository.findById(detail.getMateriel().getIdMateriel())
                    .orElseThrow(() -> new IllegalStateException(
                            "Aucune fiche de stock pour le matériel " + detail.getMateriel().getReference()));

            if (stock.getQuantiteDisponible() < detail.getQuantite()) {
                String refMission = (sortie.getMission() != null) ? sortie.getMission().getReference()
                        : (sortie.getIntervention() != null && sortie.getIntervention().getMission() != null)
                        ? sortie.getIntervention().getMission().getReference() : "N/A";

                String message = "Alerte Rupture de Stock : Le matériel " + detail.getMateriel().getNom()
                        + " (Réf: " + detail.getMateriel().getReference() + ") est en rupture de stock suite à "
                        + "une demande pour la mission " + refMission
                        + " (disponible: " + stock.getQuantiteDisponible()
                        + ", demandé: " + detail.getQuantite() + ").";

                notificationHelperService.notifierTousLesAdmins(validateur, message, "RUPTURE_STOCK");

                throw new IllegalStateException(
                        "Stock insuffisant pour " + detail.getMateriel().getReference()
                                + " (disponible: " + stock.getQuantiteDisponible()
                                + ", demandé: " + detail.getQuantite() + ")");
            }
        }

        for (DetailSortieMateriel detail : sortie.getDetails()) {
            StockMateriel stock = stockMaterielRepository.findById(detail.getMateriel().getIdMateriel()).get();
            stock.setQuantiteDisponible(stock.getQuantiteDisponible() - detail.getQuantite());
            stock.setQuantiteReservee(stock.getQuantiteReservee() + detail.getQuantite());
            stockMaterielRepository.save(stock);
        }

        sortie.setStatut("Validée");
        sortie.setValidateur(validateur);
        SortieMateriel saved = sortieMaterielRepository.save(sortie);

        // ✅ Mettre à jour les MissionMateriel correspondants à APPROUVE
        if (sortie.getMission() != null) {
            List<MissionMateriel> missionMateriels = missionMaterielRepository.findByMission_IdMission(sortie.getMission().getIdMission());
            for (MissionMateriel mm : missionMateriels) {
                mm.setStatut("APPROUVE");
                mm.setDateValidation(LocalDateTime.now());
            }
            missionMaterielRepository.saveAll(missionMateriels);
        }

        // ✅ ENVOYER UNE NOTIFICATION AU TECHNICIEN
        if (sortie.getTechnicien() != null) {
            String refMission = sortie.getMission() != null ? sortie.getMission().getReference()
                    : (sortie.getIntervention() != null && sortie.getIntervention().getMission() != null)
                    ? sortie.getIntervention().getMission().getReference() : "N/A";
            String message = "Votre demande de matériel pour la mission " + refMission
                    + " a été approuvée.";
            notificationHelperService.envoyerNotification(validateur, sortie.getTechnicien(), message, "SORTIE_APPROUVEE");
        }

        return toDto(saved);
    }

    @Transactional
    public SortieMaterielDto rejeter(Integer idSortie, Integer idValidateur, RejeterSortieRequest request) {
        SortieMateriel sortie = sortieMaterielRepository.findById(idSortie)
                .orElseThrow(() -> new EntityNotFoundException("Sortie introuvable."));

        if (!"En attente".equals(sortie.getStatut())) {
            throw new IllegalStateException("Cette demande a déjà été traitée.");
        }

        Utilisateur validateur = utilisateurRepository.findById(idValidateur)
                .orElseThrow(() -> new EntityNotFoundException("Validateur introuvable."));

        sortie.setStatut("Rejetée");
        sortie.setMotif(request.getMotifRejet());
        sortie.setValidateur(validateur);
        SortieMateriel saved = sortieMaterielRepository.save(sortie);

        // ✅ Mettre à jour les MissionMateriel correspondants à REJETE + envoyer notification
        if (sortie.getMission() != null) {
            List<MissionMateriel> missionMateriels = missionMaterielRepository.findByMission_IdMission(sortie.getMission().getIdMission());
            for (MissionMateriel mm : missionMateriels) {
                mm.setStatut("REJETE");
                mm.setMotifRejet(request.getMotifRejet());
                mm.setDateValidation(LocalDateTime.now());
            }
            missionMaterielRepository.saveAll(missionMateriels);
        }

        // ✅ ENVOYER UNE NOTIFICATION AU TECHNICIEN
        if (sortie.getTechnicien() != null) {
            String refMission = sortie.getMission() != null ? sortie.getMission().getReference()
                    : (sortie.getIntervention() != null && sortie.getIntervention().getMission() != null)
                    ? sortie.getIntervention().getMission().getReference() : "N/A";
            String message = "Votre demande de matériel pour la mission " + refMission
                    + " a été rejetée. Motif : " + request.getMotifRejet();
            notificationHelperService.envoyerNotification(validateur, sortie.getTechnicien(), message, "SORTIE_REJETEE");
        }

        return toDto(saved);
    }
}