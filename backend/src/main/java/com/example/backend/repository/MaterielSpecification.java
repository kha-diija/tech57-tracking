package com.example.backend.repository;

import com.example.backend.entity.Materiel;
import org.springframework.data.jpa.domain.Specification;

/**
 * Construit dynamiquement les critères de recherche / filtre pour le module
 * "Gestion des matériels" :
 *   - recherche texte libre sur référence, nom, numéro de série, code QR
 *   - filtre par état (Neuf / En service / En panne / Retiré)
 *   - filtre par catégorie
 *   - option "top-level uniquement" (masque les composants de kit dans la
 *     liste principale, ils s'affichent seulement dans la fiche détail du kit)
 */
public class MaterielSpecification {

    public static Specification<Materiel> search(String texte) {
        if (texte == null || texte.isBlank()) {
            return null;
        }
        String like = "%" + texte.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("reference")), like),
                cb.like(cb.lower(root.get("nom")), like),
                cb.like(cb.lower(cb.coalesce(root.get("numeroSerie"), "")), like),
                cb.like(cb.lower(cb.coalesce(root.get("codeQr"), "")), like)
        );
    }

    public static Specification<Materiel> hasEtat(String etat) {
        if (etat == null || etat.isBlank()) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("etat"), etat);
    }

    public static Specification<Materiel> hasCategorie(Integer idCategorie) {
        if (idCategorie == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("categorie").get("idCategorie"), idCategorie);
    }

    public static Specification<Materiel> hasEtablissement(Integer idEtablissement) {
        if (idEtablissement == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("etablissement").get("idEtablissement"), idEtablissement);
    }

    /** Ne garde que les matériels "racine" : simples ou kits, jamais un composant. */
    public static Specification<Materiel> topLevelOnly() {
        return (root, query, cb) -> cb.isNull(root.get("materielParent"));
    }

    public static Specification<Materiel> combine(String texte, String etat,
                                                    Integer idCategorie, Integer idEtablissement,
                                                    boolean topLevelOnly) {
        Specification<Materiel> spec = Specification.where(null);

        Specification<Materiel> s;
        if ((s = search(texte)) != null) spec = spec.and(s);
        if ((s = hasEtat(etat)) != null) spec = spec.and(s);
        if ((s = hasCategorie(idCategorie)) != null) spec = spec.and(s);
        if ((s = hasEtablissement(idEtablissement)) != null) spec = spec.and(s);
        if (topLevelOnly) spec = spec.and(topLevelOnly());

        return spec;
    }
}