package com.example.backend.service.gestionnairestock;

import com.example.backend.dto.gestionnairestock.SortieARegulariserDto;
import com.example.backend.dto.gestionnairestock.SortieARegulariserDto.LigneSortieDto;
import com.example.backend.dto.gestionnairestock.ValiderRetourRequest;
import com.example.backend.dto.gestionnairestock.ValiderRetourRequest.LigneRetourRequest;
import com.example.backend.entity.*;
import com.example.backend.repository.UtilisateurRepository;
import com.example.backend.repository.admin.RetourMaterielRepository;
import com.example.backend.repository.admin.SortieMaterielRepository;
import com.example.backend.repository.gestionnairestock.GsStockMaterielRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RetourMaterielGestionService {

    private final SortieMaterielRepository sortieMaterielRepository;
    private final RetourMaterielRepository retourMaterielRepository;
    private final GsStockMaterielRepository stockMaterielRepository;
    private final UtilisateurRepository utilisateurRepository;

    public RetourMaterielGestionService(
            SortieMaterielRepository sortieMaterielRepository,
            RetourMaterielRepository retourMaterielRepository,
            GsStockMaterielRepository stockMaterielRepository,
            UtilisateurRepository utilisateurRepository) {
        this.sortieMaterielRepository = sortieMaterielRepository;
        this.retourMaterielRepository = retourMaterielRepository;
        this.stockMaterielRepository = stockMaterielRepository;
        this.utilisateurRepository = utilisateurRepository;
    }

    // ------------------------------------------------------------------
    // Liste des sorties à régulariser
    // ------------------------------------------------------------------
    //
    // une SortieMateriel est créée automatiquement lors de la
    // création d'une mission (voir MissionInstallationService.creerSortieMaterielAutomatically)
    // et est liée à la MISSION (sortie.setMission(mission)), PAS à une intervention
    // précise. Une mission pouvant contenir plusieurs interventions, c'est donc le
    // statut de la MISSION ("Terminée", calculé automatiquement dans
    // MissionInstallationService.recalculerStatut() une fois TOUTES les
    // interventions "Clôturée") qui doit être vérifié ici, et non plus le statut
    // d'une intervention isolée.
    @Transactional(readOnly = true)
    public List<SortieARegulariserDto> listerARegulariser() {
        List<SortieMateriel> sorties = sortieMaterielRepository.findByStatut("Validée").stream()
                .filter(s -> !Boolean.TRUE.equals(s.getRetourTraite()))
                .filter(this::missionTerminee)
                .collect(Collectors.toList());

        return sorties.stream().map(this::toRegulariserDto).collect(Collectors.toList());
    }

    /**
     * Vérifie que la mission liée à la sortie est "Terminée".
     * Gère à la fois :
     * - le cas normal : sortie liée directement à une mission
     * - un cas legacy éventuel : sortie liée uniquement à une intervention
     *   (on remonte alors à la mission de cette intervention)
     */
    private boolean missionTerminee(SortieMateriel s) {
        if (s.getMission() != null) {
            return "Terminée".equals(s.getMission().getStatut());
        }
        if (s.getIntervention() != null && s.getIntervention().getMission() != null) {
            return "Terminée".equals(s.getIntervention().getMission().getStatut());
        }
        return false;
    }

    private SortieARegulariserDto toRegulariserDto(SortieMateriel s) {
        SortieARegulariserDto dto = new SortieARegulariserDto();
        dto.setIdSortie(s.getIdSortie());
        dto.setDateSortie(s.getDateSortie());

        // ✅ On lit la référence mission directement depuis la sortie,
        // avec repli sur intervention.mission si jamais ce lien existe.
        if (s.getMission() != null) {
            dto.setMissionReference(s.getMission().getReference());
        } else if (s.getIntervention() != null && s.getIntervention().getMission() != null) {
            dto.setMissionReference(s.getIntervention().getMission().getReference());
        }

        if (s.getTechnicien() != null) {
            dto.setTechnicienId(s.getTechnicien().getId());
            dto.setTechnicienNom(s.getTechnicien().getNom() + " " + s.getTechnicien().getPrenom());
        }

        List<LigneSortieDto> lignes = s.getDetails().stream().map(d -> new LigneSortieDto(
                d.getMateriel().getIdMateriel(),
                d.getMateriel().getReference(),
                d.getMateriel().getNom(),
                d.getQuantite()
        )).collect(Collectors.toList());
        dto.setLignes(lignes);

        return dto;
    }

    // ------------------------------------------------------------------
    // Valider le retour (ventilation bon état / en panne)
    // ------------------------------------------------------------------
    @Transactional
    public void validerRetour(Integer idSortie, Integer idValidateur, ValiderRetourRequest request) {
        SortieMateriel sortie = sortieMaterielRepository.findById(idSortie)
                .orElseThrow(() -> new EntityNotFoundException("Sortie introuvable."));

        if (!"Validée".equals(sortie.getStatut())) {
            throw new IllegalStateException("Cette sortie n'est pas encore validée, impossible de traiter un retour.");
        }
        if (Boolean.TRUE.equals(sortie.getRetourTraite())) {
            throw new IllegalStateException("Cette sortie a déjà été régularisée.");
        }

        Utilisateur validateur = utilisateurRepository.findById(idValidateur)
                .orElseThrow(() -> new EntityNotFoundException("Validateur introuvable."));

        Map<Integer, Integer> quantitesSorties = sortie.getDetails().stream()
                .collect(Collectors.toMap(
                        d -> d.getMateriel().getIdMateriel(),
                        DetailSortieMateriel::getQuantite));

        Map<Integer, LigneRetourRequest> lignesParMateriel = new HashMap<>();
        for (LigneRetourRequest ligne : request.getLignes()) {
            Integer attendue = quantitesSorties.get(ligne.getIdMateriel());
            if (attendue == null) {
                throw new IllegalStateException(
                        "Le matériel " + ligne.getIdMateriel() + " ne fait pas partie de cette sortie.");
            }
            int total = ligne.getQuantiteBonEtat() + ligne.getQuantiteEnPanne();
            if (total != attendue) {
                throw new IllegalStateException(
                        "Quantité totale (" + total + ") différente de la quantité sortie (" + attendue
                                + ") pour le matériel " + ligne.getIdMateriel());
            }
            lignesParMateriel.put(ligne.getIdMateriel(), ligne);
        }

        if (!lignesParMateriel.keySet().containsAll(quantitesSorties.keySet())) {
            throw new IllegalStateException("Toutes les lignes de la sortie doivent être régularisées en une fois.");
        }

        for (DetailSortieMateriel detail : sortie.getDetails()) {
            LigneRetourRequest ligne = lignesParMateriel.get(detail.getMateriel().getIdMateriel());
            Materiel materiel = detail.getMateriel();

            StockMateriel stock = stockMaterielRepository.findById(materiel.getIdMateriel())
                    .orElseThrow(() -> new IllegalStateException(
                            "Aucune fiche de stock pour " + materiel.getReference()));

            if (ligne.getQuantiteBonEtat() > 0) {
                RetourMateriel retourBon = new RetourMateriel();
                retourBon.setMateriel(materiel);
                retourBon.setTechnicien(sortie.getTechnicien());
                retourBon.setIntervention(sortie.getIntervention());
                retourBon.setSortieMateriel(sortie);
                retourBon.setQuantite(ligne.getQuantiteBonEtat());
                retourBon.setEtatMateriel("Bon état");
                retourBon.setStatut("Validée");
                retourBon.setValidateur(validateur);
                retourMaterielRepository.save(retourBon);

                stock.setQuantiteDisponible(stock.getQuantiteDisponible() + ligne.getQuantiteBonEtat());
                stock.setQuantiteReservee(stock.getQuantiteReservee() - ligne.getQuantiteBonEtat());
            }

            if (ligne.getQuantiteEnPanne() > 0) {
                RetourMateriel retourPanne = new RetourMateriel();
                retourPanne.setMateriel(materiel);
                retourPanne.setTechnicien(sortie.getTechnicien());
                retourPanne.setIntervention(sortie.getIntervention());
                retourPanne.setSortieMateriel(sortie);
                retourPanne.setQuantite(ligne.getQuantiteEnPanne());
                retourPanne.setEtatMateriel("En panne");
                retourPanne.setStatut("Validée");
                retourPanne.setValidateur(validateur);
                retourMaterielRepository.save(retourPanne);

                stock.setQuantiteEnPanne(stock.getQuantiteEnPanne() + ligne.getQuantiteEnPanne());
                stock.setQuantiteReservee(stock.getQuantiteReservee() - ligne.getQuantiteEnPanne());
            }

            stockMaterielRepository.save(stock);
        }

        sortie.setRetourTraite(true);
        sortieMaterielRepository.save(sortie);
    }
}