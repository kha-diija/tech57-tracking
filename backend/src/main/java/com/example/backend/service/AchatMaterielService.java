package com.example.backend.service;

import com.example.backend.dto.AchatMaterielDto;
import com.example.backend.dto.CreerAchatRequest;
import com.example.backend.entity.*;
import com.example.backend.repository.AchatMaterielRepository;
import com.example.backend.repository.AdministrateurRepository;
import com.example.backend.repository.GestionnaireStockRepository;
import com.example.backend.repository.adminMaterielRepository;
import com.example.backend.repository.gestionnairestock.GsStockMaterielRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AchatMaterielService {

    private final AchatMaterielRepository achatMaterielRepository;
    private final adminMaterielRepository materielRepository;
    private final GsStockMaterielRepository stockMaterielRepository;
    private final AdministrateurRepository administrateurRepository;
    private final GestionnaireStockRepository gestionnaireStockRepository;

    public AchatMaterielService(
            AchatMaterielRepository achatMaterielRepository,
            adminMaterielRepository materielRepository,
            GsStockMaterielRepository stockMaterielRepository,
            AdministrateurRepository administrateurRepository,
            GestionnaireStockRepository gestionnaireStockRepository) {
        this.achatMaterielRepository = achatMaterielRepository;
        this.materielRepository = materielRepository;
        this.stockMaterielRepository = stockMaterielRepository;
        this.administrateurRepository = administrateurRepository;
        this.gestionnaireStockRepository = gestionnaireStockRepository;
    }

    public List<AchatMaterielDto> lister() {
        return achatMaterielRepository.findAllByOrderByDateAchatDesc()
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    public List<AchatMaterielDto> listerParMateriel(Integer idMateriel) {
        return achatMaterielRepository.findByMateriel_IdMaterielOrderByDateAchatDesc(idMateriel)
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    /**
     * @param idUtilisateurConnecte id de l'utilisateur authentifié
     * @param roleConnecte "ADMINISTRATEUR" ou "GESTIONNAIRE_STOCK", pour savoir
     *                     dans quelle colonne (acheteurAdmin / acheteurGestionnaire)
     *                     enregistrer l'auteur de l'achat.
     */
    @Transactional
    public AchatMaterielDto creerAchat(CreerAchatRequest request, Integer idUtilisateurConnecte, String roleConnecte) {
        Materiel materiel = materielRepository.findById(request.getIdMateriel())
                .orElseThrow(() -> new EntityNotFoundException("Matériel introuvable."));

        AchatMateriel achat = new AchatMateriel();
        achat.setMateriel(materiel);
        achat.setQuantite(request.getQuantite());
        achat.setFournisseur(request.getFournisseur());
        achat.setNumeroFacture(request.getNumeroFacture());
        achat.setPrixUnitaireHt(request.getPrixUnitaireHt());

        if ("ADMINISTRATEUR".equals(roleConnecte)) {
            Administrateur admin = administrateurRepository.findById(idUtilisateurConnecte)
                    .orElseThrow(() -> new EntityNotFoundException("Administrateur introuvable."));
            achat.setAcheteurAdmin(admin);
        } else if ("GESTIONNAIRE_STOCK".equals(roleConnecte)) {
            GestionnaireStock gestionnaire = gestionnaireStockRepository.findById(idUtilisateurConnecte)
                    .orElseThrow(() -> new EntityNotFoundException("Gestionnaire de stock introuvable."));
            achat.setAcheteurGestionnaire(gestionnaire);
        } else {
            throw new IllegalStateException("Rôle non autorisé à créer un achat.");
        }

        AchatMateriel saved = achatMaterielRepository.save(achat);

        // Mise à jour automatique du stock
        StockMateriel stock = stockMaterielRepository.findById(materiel.getIdMateriel())
                .orElseGet(() -> {
                    StockMateriel nouveau = new StockMateriel();
                    nouveau.setMateriel(materiel);
                    nouveau.setQuantiteDisponible(0);
                    nouveau.setQuantiteReservee(0);
                    nouveau.setQuantiteEnPanne(0);
                    return nouveau;
                });
        stock.setQuantiteDisponible(stock.getQuantiteDisponible() + request.getQuantite());
        stockMaterielRepository.save(stock);

        return toDto(saved);
    }

    private AchatMaterielDto toDto(AchatMateriel a) {
        String acheteurNom = null;
        if (a.getAcheteurAdmin() != null) {
            acheteurNom = a.getAcheteurAdmin().getNom() + " " + a.getAcheteurAdmin().getPrenom();
        } else if (a.getAcheteurGestionnaire() != null) {
            acheteurNom = a.getAcheteurGestionnaire().getNom() + " " + a.getAcheteurGestionnaire().getPrenom();
        }

        return AchatMaterielDto.builder()
                .idAchat(a.getIdAchat())
                .numeroFacture(a.getNumeroFacture())
                .fournisseur(a.getFournisseur())
                .quantite(a.getQuantite())
                .prixUnitaireHt(a.getPrixUnitaireHt())
                .dateAchat(a.getDateAchat())
                .idMateriel(a.getMateriel().getIdMateriel())
                .materielReference(a.getMateriel().getReference())
                .materielNom(a.getMateriel().getNom())
                .acheteurNom(acheteurNom)
                .build();
    }
}