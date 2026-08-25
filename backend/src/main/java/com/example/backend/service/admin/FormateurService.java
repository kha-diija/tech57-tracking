package com.example.backend.service.admin;

import com.example.backend.dto.admin.etablissement.FormateurRequest;
import com.example.backend.dto.admin.etablissement.FormateurResponse;
import com.example.backend.entity.Etablissement;
import com.example.backend.entity.Observateur;
import com.example.backend.repository.admin.EtablissementRepository;
import com.example.backend.repository.admin.ObservateurRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
    public class FormateurService {

        private final ObservateurRepository observateurRepository;
        private final EtablissementRepository etablissementRepository;
        private final PasswordEncoder passwordEncoder;

        public FormateurService(ObservateurRepository observateurRepository,
                                EtablissementRepository etablissementRepository,
                                PasswordEncoder passwordEncoder) {
            this.observateurRepository = observateurRepository;
            this.etablissementRepository = etablissementRepository;
            this.passwordEncoder = passwordEncoder;
        }

    public List<FormateurResponse> getByEtablissement(Integer idEtablissement) {
        return observateurRepository.findByEtablissement_IdEtablissement(idEtablissement)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public long countByEtablissement(Integer idEtablissement) {
        return observateurRepository.countByEtablissement_IdEtablissement(idEtablissement);
    }

    public FormateurResponse create(Integer idEtablissement, FormateurRequest req) {
        Etablissement etab = etablissementRepository.findById(idEtablissement)
                .orElseThrow(() -> new NoSuchElementException("Établissement introuvable"));

        Observateur obs = new Observateur();
        obs.setNom(req.getNom());
        obs.setPrenom(req.getPrenom());
        obs.setTelephone(req.getTelephone());
        obs.setAdresse(req.getAdresse());
        obs.setEtablissement(etab);

        obs.setEmail("formateur." + UUID.randomUUID() + "@interne.local");
        // Mot de passe aléatoire haché — le formateur ne se connecte pas avec ce compte,
        // c'est juste requis par la contrainte utilisateur_check (auth_provider = LOCAL)
        obs.setMotDePasse(passwordEncoder.encode(UUID.randomUUID().toString()));
        obs.setCompteActif(true);

        return toResponse(observateurRepository.save(obs));
    }

    public FormateurResponse update(Integer idFormateur, FormateurRequest req) {
        Observateur obs = observateurRepository.findById(idFormateur)
                .orElseThrow(() -> new NoSuchElementException("Formateur introuvable"));

        obs.setNom(req.getNom());
        obs.setPrenom(req.getPrenom());
        obs.setTelephone(req.getTelephone());
        obs.setAdresse(req.getAdresse());

        return toResponse(observateurRepository.save(obs));
    }

    public void delete(Integer idFormateur) {
        if (!observateurRepository.existsById(idFormateur)) {
            throw new NoSuchElementException("Formateur introuvable");
        }
        observateurRepository.deleteById(idFormateur);
    }

    private FormateurResponse toResponse(Observateur obs) {
        FormateurResponse dto = new FormateurResponse();
        dto.setIdFormateur(obs.getId());
        dto.setNom(obs.getNom());
        dto.setPrenom(obs.getPrenom());
        dto.setTelephone(obs.getTelephone());
        dto.setAdresse(obs.getAdresse());
        return dto;
    }
}