package com.example.backend.service.admin;

import com.example.backend.dto.admin.Mission.MissionRequestDTO;
import com.example.backend.dto.admin.Mission.MissionResponseDTO;
import com.example.backend.dto.admin.Mission.MissionMaterielDTO;
import com.example.backend.entity.*;
import com.example.backend.repository.admin.*;
import com.example.backend.repository.UtilisateurRepository;
import com.example.backend.repository.admin.SortieMaterielRepository;
import com.example.backend.repository.admin.MissionMaterielRepository;
import com.example.backend.service.NotificationHelperService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MissionInstallationService {

    @Autowired
    private MissionInstallationRepository missionRepository;

    @Autowired
    private EtablissementRepository etablissementRepository;

    @Autowired
    private EquipeTechniqueRepository equipeTechniqueRepository;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private InterventionRepository interventionRepository;

    @Autowired
    private RapportRepository rapportRepository;

    @Autowired
    private PhotoRepository photoRepository;

    @Autowired
    private AttestationRepository attestationRepository;

    @Autowired
    private ChecklistEquipementRepository checklistEquipementRepository;

    @Autowired
    private NotificationHelperService notificationHelperService;

    // ✅ NOUVEAUX AUTOWIRED
    @Autowired
    private MaterielRepository materielRepository;

    @Autowired
    private MissionMaterielRepository missionMaterielRepository;

    @Autowired
    private SortieMaterielRepository sortieMaterielRepository;

    @Autowired
    private TechnicienRepository technicienRepository;

    @Transactional
    public List<MissionResponseDTO> getAllMissions() {
        return missionRepository.findAll().stream()
                .map(MissionResponseDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<MissionResponseDTO> getMissionsByTechnicienEquipe(Integer idTechnicien) {
        EquipeTechnique equipe = equipeTechniqueRepository.findByMembreId(idTechnicien)
                .orElseThrow(() -> new RuntimeException("Ce technicien n'est affecté à aucune équipe technique."));

        return missionRepository.findByEquipeIdEquipe(equipe.getIdEquipe()).stream()
                .map(MissionResponseDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public MissionResponseDTO getMissionById(Integer id) {
        MissionInstallation mission = missionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mission introuvable avec l'ID : " + id));
        return new MissionResponseDTO(mission);
    }

    @Transactional
    public MissionResponseDTO createMission(MissionRequestDTO dto) {
        try {
            MissionInstallation mission = new MissionInstallation();
            mapDtoToEntity(dto, mission);
            MissionInstallation saved = missionRepository.save(mission);
            return new MissionResponseDTO(saved);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("ERREUR EXACTE : " + e.getMessage(), e);
        }
    }

    @Transactional
    public MissionResponseDTO createMissionByTechnicien(MissionRequestDTO dto, Integer idTechnicien) {
        Utilisateur technicien = utilisateurRepository.findById(idTechnicien)
                .orElseThrow(() -> new RuntimeException("Technicien introuvable"));

        EquipeTechnique equipe = equipeTechniqueRepository.findByMembreId(idTechnicien)
                .orElseThrow(() -> new RuntimeException("Impossible de créer la mission : vous n'êtes rattaché à aucune équipe."));

        dto.setIdEquipe(equipe.getIdEquipe());

        // ✅ Le statut est TOUJOURS PROPOSEE quand un technicien crée une mission
        dto.setStatut("PROPOSEE");

        // Créer la mission avec les matériels
        MissionInstallation mission = new MissionInstallation();
        mapDtoToEntity(dto, mission);

        // ✅ Sauvegarder la mission
        MissionInstallation savedMission = missionRepository.save(mission);

        // ✅ Générer automatiquement une SortieMateriel
        if (dto.getMateriels() != null && !dto.getMateriels().isEmpty()) {
            creerSortieMaterielAutomatically(savedMission, dto.getMateriels(), technicien);
        }

        // ✅ Envoyer une notification aux admins
        String message = "Le technicien " + technicien.getPrenom() + " " + technicien.getNom() +
                " a proposé une nouvelle mission : \"" + dto.getTitre() + "\" pour son équipe (" + equipe.getNomEquipe() + ").";

        notificationHelperService.notifierTousLesAdmins(technicien, message, "MISSION_PROPOSEE");

        return new MissionResponseDTO(savedMission);
    }

    @Transactional
    public MissionResponseDTO updateMission(Integer id, MissionRequestDTO dto) {
        MissionInstallation mission = missionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mission introuvable avec l'ID : " + id));

        // ✅ Mettre à jour les champs de base
        mission.setReference(dto.getReference());
        mission.setTitre(dto.getTitre());
        if (dto.getStatut() != null && !dto.getStatut().isEmpty()) {
            mission.setStatut(dto.getStatut());
        }
        mission.setBudgetPropose(dto.getBudgetPropose());

        // ✅ Mettre à jour l'établissement
        Etablissement etab = etablissementRepository.findById(dto.getIdEtablissement())
                .orElseThrow(() -> new RuntimeException("Établissement introuvable"));
        mission.setEtablissement(etab);

        // ✅ Mettre à jour l'admin
        if (dto.getIdAdministrateur() != null) {
            Utilisateur user = utilisateurRepository.findById(dto.getIdAdministrateur())
                    .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
            if (user instanceof Administrateur) {
                mission.setAdministrateur((Administrateur) user);
            } else {
                mission.setAdministrateur(null);
            }
        }

        // ✅ Mettre à jour l'équipe (si non fourni, on garde l'existant)
        if (dto.getIdEquipe() != null) {
            EquipeTechnique equipe = equipeTechniqueRepository.findById(dto.getIdEquipe())
                    .orElseThrow(() -> new RuntimeException("Équipe technique introuvable"));
            mission.setEquipe(equipe);
        }

        // ✅ GESTION DES MATÉRIELS DE LA MISSION
        // 1️⃣ Supprimer les anciens matériels de la mission
        if (mission.getMateriels() != null && !mission.getMateriels().isEmpty()) {
            missionMaterielRepository.deleteAll(mission.getMateriels());
            mission.getMateriels().clear();
        }

        // 2️⃣ Ajouter les nouveaux matériels à la mission
        if (dto.getMateriels() != null && !dto.getMateriels().isEmpty()) {
            for (MissionMaterielDTO materielDTO : dto.getMateriels()) {
                Materiel materiel = materielRepository.findById(materielDTO.getIdMateriel())
                        .orElseThrow(() -> new RuntimeException("Matériel introuvable avec ID : " + materielDTO.getIdMateriel()));

                MissionMateriel missionMateriel = new MissionMateriel();
                missionMateriel.setMission(mission);
                missionMateriel.setMateriel(materiel);
                missionMateriel.setQuantite(materielDTO.getQuantite() != null ? materielDTO.getQuantite() : 1);
                missionMateriel.setStatut("PROPOSE");
                mission.getMateriels().add(missionMateriel);
            }
        }

        // ✅ Sauvegarder la mission
        MissionInstallation updated = missionRepository.save(mission);

        // ✅ METTRE À JOUR LA SORTIE DE MATÉRIEL ASSOCIÉE
        mettreAJourSortieMateriel(updated, dto.getMateriels());

        // ✅ Recharger la mission pour la réponse
        MissionInstallation refreshed = missionRepository.findById(updated.getIdMission())
                .orElseThrow(() -> new RuntimeException("Mission introuvable après mise à jour"));

        return new MissionResponseDTO(refreshed);
    }

    /**
     * Met à jour la SortieMateriel associée à la mission
     */
    private void mettreAJourSortieMateriel(MissionInstallation mission, List<MissionMaterielDTO> materielsDTO) {
        try {
            // 1. Récupérer la SortieMateriel associée à la mission
            List<SortieMateriel> sorties = sortieMaterielRepository.findByMissionIdMission(mission.getIdMission());

            if (sorties.isEmpty()) {
                // Si pas de sortie, en créer une nouvelle (si des matériels sont présents)
                if (materielsDTO != null && !materielsDTO.isEmpty()) {
                    // Récupérer le technicien depuis l'équipe
                    Utilisateur technicien = null;
                    if (mission.getEquipe() != null && !mission.getEquipe().getMembres().isEmpty()) {
                        technicien = mission.getEquipe().getMembres().get(0);
                    }
                    if (technicien != null) {
                        creerSortieMaterielAutomatically(mission, materielsDTO, technicien);
                    }
                }
                return;
            }

            SortieMateriel sortie = sorties.get(0);

            // 2. Supprimer les anciens détails de la sortie
            if (sortie.getDetails() != null && !sortie.getDetails().isEmpty()) {
                sortie.getDetails().clear();
            }

            // 3. Ajouter les nouveaux matériels à la sortie
            if (materielsDTO != null && !materielsDTO.isEmpty()) {
                for (MissionMaterielDTO materielDTO : materielsDTO) {
                    Materiel materiel = materielRepository.findById(materielDTO.getIdMateriel())
                            .orElseThrow(() -> new RuntimeException("Matériel introuvable"));

                    DetailSortieMateriel detail = new DetailSortieMateriel();
                    detail.setSortieMateriel(sortie);
                    detail.setMateriel(materiel);
                    detail.setQuantite(materielDTO.getQuantite() != null ? materielDTO.getQuantite() : 1);
                    sortie.getDetails().add(detail);
                }
            }

            // 4. Sauvegarder la sortie mise à jour
            sortieMaterielRepository.save(sortie);

        } catch (Exception e) {
            e.printStackTrace();
            // Ne pas bloquer la mise à jour de la mission si la sortie échoue
            System.err.println("Erreur lors de la mise à jour de la SortieMateriel: " + e.getMessage());
        }
    }

    @Transactional
    public void deleteMission(Integer id, boolean force) {
        MissionInstallation mission = missionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Impossible de supprimer, mission introuvable ID : " + id));

        List<Intervention> interventions = interventionRepository.findByMissionIdMission(id);
        long nbInterventions = interventions.size();

        if (nbInterventions > 0 && !force) {
            throw new IllegalStateException(
                    "Cette mission est liée à " + nbInterventions + " intervention(s). " +
                            "Toutes les données associées (interventions, rapports, photos, matériel...) seront définitivement supprimées si vous continuez."
            );
        }

        if (force) {
            for (Intervention intervention : interventions) {
                rapportRepository.deleteByIntervention(intervention);
                photoRepository.deleteByIntervention(intervention);
                attestationRepository.deleteByIntervention(intervention);
                checklistEquipementRepository.deleteByIntervention(intervention);
            }
            rapportRepository.deleteByMission(mission);
            interventionRepository.deleteAll(interventions);
        }

        missionRepository.deleteById(id);
    }

    @Transactional
    public void recalculerStatut(Integer idMission) {
        MissionInstallation mission = missionRepository.findById(idMission)
                .orElseThrow(() -> new RuntimeException("Mission introuvable avec l'ID : " + idMission));

        List<Intervention> interventions = interventionRepository.findByMissionIdMission(idMission);

        if (interventions.isEmpty()) {
            return;
        }

        boolean toutesCloturees = interventions.stream()
                .allMatch(i -> "Clôturée".equals(i.getStatut()));

        if (toutesCloturees) {
            mission.setStatut("Terminée");
            missionRepository.save(mission);
            return;
        }

        boolean toutesPlanifiees = interventions.stream()
                .allMatch(i -> "Planifiée".equals(i.getStatut()));

        if (toutesPlanifiees) {
            mission.setStatut("Planifiée");
            missionRepository.save(mission);
            return;
        }

        mission.setStatut("En cours");
        missionRepository.save(mission);
    }


    // ✅ Dans approuverMission()
    @Transactional
    public MissionResponseDTO approuverMission(Integer idMission, Integer idAdmin) {
        MissionInstallation mission = missionRepository.findById(idMission)
                .orElseThrow(() -> new RuntimeException("Mission introuvable"));

        // ✅ VÉRIFICATION : Vérifier si la mission a des matériels rejetés
        List<MissionMateriel> missionMateriels = missionMaterielRepository.findByMission_IdMission(idMission);
        boolean aDesRejetes = missionMateriels.stream()
                .anyMatch(mm -> "REJETE".equals(mm.getStatut()));

        if (aDesRejetes) {
            throw new IllegalStateException(
                    "Impossible d'approuver cette mission car un ou plusieurs matériels ont été rejetés."
            );
        }

        mission.setStatut("Planifiée");
        MissionInstallation updated = missionRepository.save(mission);

        // Récupérer l'admin connecté
        Utilisateur admin = utilisateurRepository.findById(idAdmin)
                .orElseThrow(() -> new RuntimeException("Admin introuvable"));

        // Envoyer une notification au technicien avec l'admin comme expéditeur
        if (updated.getEquipe() != null && !updated.getEquipe().getMembres().isEmpty()) {
            Technicien technicien = updated.getEquipe().getMembres().get(0);
            String message = "Votre mission '" + updated.getTitre() + "' a été approuvée.";
            notificationHelperService.envoyerNotification(admin, technicien, message, "MISSION_APPROUVEE");
        }

        return new MissionResponseDTO(updated);
    }

    @Transactional
    public void rejeterMission(Integer idMission, String motif, Integer idAdmin) {
        MissionInstallation mission = missionRepository.findById(idMission)
                .orElseThrow(() -> new RuntimeException("Mission introuvable"));

        // Récupérer l'admin connecté
        Utilisateur admin = utilisateurRepository.findById(idAdmin)
                .orElseThrow(() -> new RuntimeException("Admin introuvable"));

        // Récupérer le technicien (via l'équipe)
        Technicien technicien = null;
        if (mission.getEquipe() != null && !mission.getEquipe().getMembres().isEmpty()) {
            technicien = mission.getEquipe().getMembres().get(0);
        }

        // Supprimer les sorties et matériels
        List<SortieMateriel> sorties = sortieMaterielRepository.findByMissionIdMission(idMission);
        for (SortieMateriel sortie : sorties) {
            sortie.getDetails().clear();
            sortieMaterielRepository.save(sortie);
            sortieMaterielRepository.delete(sortie);
        }

        missionMaterielRepository.deleteByMission_IdMission(idMission);
        missionRepository.delete(mission);

        // Envoyer une notification avec l'admin comme expéditeur
        if (technicien != null) {
            String message = "Votre mission '" + mission.getTitre() + "' a été rejetée. Motif : " + motif;
            notificationHelperService.envoyerNotification(admin, technicien, message, "MISSION_REJETEE");
        }
    }

    private void mapDtoToEntity(MissionRequestDTO dto, MissionInstallation mission) {
        mission.setReference(dto.getReference());
        mission.setTitre(dto.getTitre());
        if (dto.getStatut() != null && !dto.getStatut().isEmpty()) {
            mission.setStatut(dto.getStatut());
        }
        mission.setBudgetPropose(dto.getBudgetPropose());

        Etablissement etab = etablissementRepository.findById(dto.getIdEtablissement())
                .orElseThrow(() -> new RuntimeException("Établissement introuvable"));
        mission.setEtablissement(etab);

        // Gestion Admin
        if (dto.getIdAdministrateur() != null) {
            Utilisateur user = utilisateurRepository.findById(dto.getIdAdministrateur())
                    .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
            if (user instanceof Administrateur) {
                mission.setAdministrateur((Administrateur) user);
            } else {
                mission.setAdministrateur(null);
            }
        } else if (mission.getIdMission() != null) {
            // On garde l'admin existant
        } else {
            mission.setAdministrateur(null);
        }

        // Gestion Équipe
        if (dto.getIdEquipe() != null) {
            EquipeTechnique equipe = equipeTechniqueRepository.findById(dto.getIdEquipe())
                    .orElseThrow(() -> new RuntimeException("Équipe technique introuvable"));
            mission.setEquipe(equipe);
        } else if (mission.getIdMission() == null) {
            mission.setEquipe(null);
        }

        // ✅ GESTION DES MATÉRIELS
        if (dto.getMateriels() != null && !dto.getMateriels().isEmpty()) {
            for (MissionMaterielDTO materielDTO : dto.getMateriels()) {
                Materiel materiel = materielRepository.findById(materielDTO.getIdMateriel())
                        .orElseThrow(() -> new RuntimeException("Matériel introuvable avec ID : " + materielDTO.getIdMateriel()));

                MissionMateriel missionMateriel = new MissionMateriel();
                missionMateriel.setMission(mission);
                missionMateriel.setMateriel(materiel);
                missionMateriel.setQuantite(materielDTO.getQuantite() != null ? materielDTO.getQuantite() : 1);
                missionMateriel.setStatut("PROPOSE");

                mission.getMateriels().add(missionMateriel);
            }
        }
    }

    // ✅ MÉTHODE POUR CRÉER UNE SORTIE AUTOMATIQUEMENT
    private void creerSortieMaterielAutomatically(MissionInstallation mission, List<MissionMaterielDTO> materiels, Utilisateur technicien) {
        try {
            SortieMateriel sortie = new SortieMateriel();
            sortie.setDateSortie(LocalDateTime.now());
            sortie.setStatut("En attente");
            sortie.setMission(mission);
            sortie.setLieuIntervention(mission.getEtablissement() != null ? mission.getEtablissement().getDesignation() : null);

            // Trouver le Technicien
            if (technicien instanceof Technicien) {
                sortie.setTechnicien((Technicien) technicien);
            }

            // Ajouter les détails de sortie
            for (MissionMaterielDTO materielDTO : materiels) {
                Materiel materiel = materielRepository.findById(materielDTO.getIdMateriel())
                        .orElseThrow(() -> new RuntimeException("Matériel introuvable"));

                DetailSortieMateriel detail = new DetailSortieMateriel();
                detail.setSortieMateriel(sortie);
                detail.setMateriel(materiel);
                detail.setQuantite(materielDTO.getQuantite() != null ? materielDTO.getQuantite() : 1);
                sortie.getDetails().add(detail);
            }

            sortieMaterielRepository.save(sortie);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}