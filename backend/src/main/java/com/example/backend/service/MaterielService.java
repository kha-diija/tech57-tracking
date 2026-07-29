package com.example.backend.service;

import com.example.backend.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MaterielService {

    Page<MaterielDTO> rechercher(String texte, String etat, Integer idCategorie,
                                  Integer idEtablissement, boolean topLevelOnly, Pageable pageable);

    MaterielDTO getById(Integer id);

    MaterielDTO creerSimple(MaterielRequest request);

    MaterielDTO creerKit(KitRequest request);

    MaterielDTO modifier(Integer id, MaterielRequest request);

    void supprimer(Integer id);

    MaterielDTO changerEtat(Integer id, String nouvelEtat);

    MaterielDTO marquerEnMaintenance(Integer id);

    MaterielDTO ajouterComposant(Integer idKit, ComposantRequest request);

    void retirerComposant(Integer idComposant);

    String regenererCodeQr(Integer id);
}