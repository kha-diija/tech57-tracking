package com.example.backend.service;

import com.example.backend.dto.*;
import com.example.backend.entity.CategorieMateriel;
import com.example.backend.entity.Etablissement;
import com.example.backend.entity.Materiel;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class MaterielServiceImpl implements MaterielService {

    private static final List<String> COMPOSANTS_KIT_VEX_GO_DEFAUT = List.of(
            "Guide d'utilisation",
            "Sachet des matériaux",
            "Câbles USB",
            "Chargeur",
            "Batterie VEX GO"
    );

    private static final Set<String> ETATS_VALIDES = Set.of("Neuf", "En service", "En panne", "Retiré");

    private final adminMaterielRepository materielRepository;
    private final CategorieMaterielRepository categorieMaterielRepository;
    private final adminEtablissementRepository etablissementRepository;
    private final MaintenanceRepository maintenanceRepository;

    // ---------------------------------------------------------------
    // Lecture / recherche
    // ---------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public Page<MaterielDTO> rechercher(String texte, String etat, Integer idCategorie,
                                         Integer idEtablissement, boolean topLevelOnly, Pageable pageable) {
        Specification<Materiel> spec = MaterielSpecification.combine(
                texte, etat, idCategorie, idEtablissement, topLevelOnly);
        return materielRepository.findAll(spec, pageable)
                .map(m -> toDto(m, false));
    }

    @Override
    @Transactional(readOnly = true)
    public MaterielDTO getById(Integer id) {
        Materiel m = findMaterielOrThrow(id);
        return toDto(m, true);
    }

    // ---------------------------------------------------------------
    // Création matériel simple
    // ---------------------------------------------------------------

    @Override
    public MaterielDTO creerSimple(MaterielRequest request) {
        verifierReferenceUnique(request.getReference(), null);

        CategorieMateriel categorie = findCategorieOrThrow(request.getIdCategorie());
        if (Boolean.TRUE.equals(categorie.getEstKit())) {
            throw new IllegalArgumentException(
                    "Cette catégorie est une catégorie de kit : utilisez /api/materiels/kits");
        }

        Materiel m = new Materiel();
        m.setReference(request.getReference());
        m.setNom(request.getNom());
        m.setNumeroSerie(request.getNumeroSerie());
        m.setEtat(estatOuDefaut(request.getEtat()));
        m.setCategorie(categorie);
        m.setEtablissement(resoudreEtablissement(request.getIdEtablissement()));
        m.setCodeQr(request.getCodeQr() != null && !request.getCodeQr().isBlank()
                ? request.getCodeQr()
                : genererCodeQr(request.getReference()));

        return toDto(materielRepository.save(m), false);
    }

    // ---------------------------------------------------------------
    // Création d'un kit composite (ex. Kit VEX GO)
    // ---------------------------------------------------------------

    @Override
    public MaterielDTO creerKit(KitRequest request) {
        verifierReferenceUnique(request.getReference(), null);

        CategorieMateriel categorie = findCategorieOrThrow(request.getIdCategorie());
        if (!Boolean.TRUE.equals(categorie.getEstKit())) {
            throw new IllegalArgumentException(
                    "La catégorie choisie n'est pas marquée comme kit (est_kit = true)");
        }

        Materiel kit = new Materiel();
        kit.setReference(request.getReference());
        kit.setNom(request.getNom());
        kit.setNumeroSerie(request.getNumeroSerie());
        kit.setEtat("Neuf");
        kit.setCategorie(categorie);
        kit.setEtablissement(resoudreEtablissement(request.getIdEtablissement()));
        kit.setCodeQr(request.getCodeQr() != null && !request.getCodeQr().isBlank()
                ? request.getCodeQr()
                : genererCodeQr(request.getReference()));

        List<String> noms = (request.getComposants() != null && !request.getComposants().isEmpty())
                ? request.getComposants()
                : COMPOSANTS_KIT_VEX_GO_DEFAUT;

        boolean heriteQr = request.getComposantsHeritentQr() == null || request.getComposantsHeritentQr();

        List<Materiel> composants = new ArrayList<>();
        int i = 1;
        for (String nomComposant : noms) {
            Materiel c = new Materiel();
            c.setReference(request.getReference() + "-C" + i++);
            c.setNom(nomComposant);
            c.setEtat("Neuf");
            c.setCategorie(categorie);
            c.setEtablissement(kit.getEtablissement());
            c.setMaterielParent(kit);
            c.setQuantiteComposant(1);
            c.setCodeQr(heriteQr ? kit.getCodeQr() : genererCodeQr(c.getReference()));
            composants.add(c);
        }
        kit.setComposants(composants);

        Materiel saved = materielRepository.save(kit); // cascade = ALL persiste aussi les composants
        return toDto(saved, true);
    }

    // ---------------------------------------------------------------
    // Modification / suppression
    // ---------------------------------------------------------------

    @Override
    public MaterielDTO modifier(Integer id, MaterielRequest request) {
        Materiel m = findMaterielOrThrow(id);
        verifierReferenceUnique(request.getReference(), id);

        CategorieMateriel categorie = findCategorieOrThrow(request.getIdCategorie());

        m.setReference(request.getReference());
        m.setNom(request.getNom());
        m.setNumeroSerie(request.getNumeroSerie());
        m.setCategorie(categorie);
        m.setEtablissement(resoudreEtablissement(request.getIdEtablissement()));
        if (request.getEtat() != null && !request.getEtat().isBlank()) {
            m.setEtat(validerEtat(request.getEtat()));
        }
        if (request.getCodeQr() != null && !request.getCodeQr().isBlank()) {
            m.setCodeQr(request.getCodeQr());
        }

        return toDto(materielRepository.save(m), true);
    }

    @Override
    public void supprimer(Integer id) {
        Materiel m = findMaterielOrThrow(id);
        // La suppression d'un kit supprime ses composants (cascade ALL côté entité)
        materielRepository.delete(m);
    }

    // ---------------------------------------------------------------
    // État / maintenance
    // ---------------------------------------------------------------

    @Override
    public MaterielDTO changerEtat(Integer id, String nouvelEtat) {
        Materiel m = findMaterielOrThrow(id);
        m.setEtat(validerEtat(nouvelEtat));
        return toDto(materielRepository.save(m), false);
    }

    @Override
    public MaterielDTO marquerEnMaintenance(Integer id) {
        return changerEtat(id, "En panne");
    }

    // ---------------------------------------------------------------
    // Gestion des composants d'un kit
    // ---------------------------------------------------------------

    @Override
    public MaterielDTO ajouterComposant(Integer idKit, ComposantRequest request) {
        Materiel kit = findMaterielOrThrow(idKit);
        if (Boolean.FALSE.equals(kit.getCategorie() != null && kit.getCategorie().getEstKit())) {
            throw new IllegalArgumentException("Ce matériel n'est pas un kit, impossible d'ajouter un composant");
        }

        Materiel composant = new Materiel();
        String reference = (request.getReference() != null && !request.getReference().isBlank())
                ? request.getReference()
                : kit.getReference() + "-C" + (kit.getComposants().size() + 1);
        verifierReferenceUnique(reference, null);

        composant.setReference(reference);
        composant.setNom(request.getNom());
        composant.setNumeroSerie(request.getNumeroSerie());
        composant.setEtat("Neuf");
        composant.setCategorie(kit.getCategorie());
        composant.setEtablissement(kit.getEtablissement());
        composant.setMaterielParent(kit);
        composant.setQuantiteComposant(request.getQuantiteComposant() != null ? request.getQuantiteComposant() : 1);
        composant.setCodeQr(request.getCodeQr() != null && !request.getCodeQr().isBlank()
                ? request.getCodeQr()
                : kit.getCodeQr());

        materielRepository.save(composant);
        return toDto(findMaterielOrThrow(idKit), true);
    }

    @Override
    public void retirerComposant(Integer idComposant) {
        Materiel composant = findMaterielOrThrow(idComposant);
        if (composant.getMaterielParent() == null) {
            throw new IllegalArgumentException("Ce matériel n'est pas un composant de kit");
        }
        materielRepository.delete(composant);
    }

    // ---------------------------------------------------------------
    // Code QR
    // ---------------------------------------------------------------

    @Override
    public String regenererCodeQr(Integer id) {
        Materiel m = findMaterielOrThrow(id);
        String nouveauCode = genererCodeQr(m.getReference());
        m.setCodeQr(nouveauCode);
        materielRepository.save(m);
        return nouveauCode;
    }

    // ---------------------------------------------------------------
    // Helpers privés
    // ---------------------------------------------------------------

    private Materiel findMaterielOrThrow(Integer id) {
        return materielRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Matériel introuvable, id=" + id));
    }

    private CategorieMateriel findCategorieOrThrow(Integer idCategorie) {
        return categorieMaterielRepository.findById(idCategorie)
                .orElseThrow(() -> new ResourceNotFoundException("Catégorie introuvable, id=" + idCategorie));
    }

    private Etablissement resoudreEtablissement(Integer idEtablissement) {
        if (idEtablissement == null) return null;
        return etablissementRepository.findById(idEtablissement)
                .orElseThrow(() -> new ResourceNotFoundException("Établissement introuvable, id=" + idEtablissement));
    }

    private void verifierReferenceUnique(String reference, Integer idAExclure) {
        materielRepository.findByReference(reference).ifPresent(existant -> {
            if (idAExclure == null || !existant.getIdMateriel().equals(idAExclure)) {
                throw new IllegalArgumentException("La référence '" + reference + "' est déjà utilisée");
            }
        });
    }

    private String estatOuDefaut(String etat) {
        return (etat == null || etat.isBlank()) ? "Neuf" : validerEtat(etat);
    }

    private String validerEtat(String etat) {
        if (!ETATS_VALIDES.contains(etat)) {
            throw new IllegalArgumentException(
                    "État invalide : " + etat + " (valeurs autorisées : " + ETATS_VALIDES + ")");
        }
        return etat;
    }

    private String genererCodeQr(String reference) {
        String base = reference.toUpperCase().replaceAll("[^A-Z0-9]", "");
        String suffixe = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "QR-" + base + "-" + suffixe;
    }

    private MaterielDTO toDto(Materiel m, boolean includeComposants) {
        boolean enMaintenance = maintenanceRepository
                .existsByMateriel_IdMaterielAndDisponibleFalse(m.getIdMateriel());

        MaterielDTO.MaterielDTOBuilder builder = MaterielDTO.builder()
                .idMateriel(m.getIdMateriel())
                .reference(m.getReference())
                .nom(m.getNom())
                .numeroSerie(m.getNumeroSerie())
                .codeQr(m.getCodeQr())
                .etat(m.getEtat())
                .idCategorie(m.getCategorie() != null ? m.getCategorie().getIdCategorie() : null)
                .nomCategorie(m.getCategorie() != null ? m.getCategorie().getNom() : null)
                .estKit(m.getCategorie() != null && Boolean.TRUE.equals(m.getCategorie().getEstKit()))
                .idEtablissement(m.getEtablissement() != null ? m.getEtablissement().getIdEtablissement() : null)
                .designationEtablissement(m.getEtablissement() != null ? m.getEtablissement().getDesignation() : null)
                .idMaterielParent(m.getMaterielParent() != null ? m.getMaterielParent().getIdMateriel() : null)
                .quantiteComposant(m.getQuantiteComposant())
                .enMaintenance(enMaintenance);

        if (includeComposants && m.getComposants() != null && !m.getComposants().isEmpty()) {
            builder.composants(m.getComposants().stream()
                    .map(c -> toDto(c, true))
                    .collect(Collectors.toList()));
        }

        return builder.build();
    }
}