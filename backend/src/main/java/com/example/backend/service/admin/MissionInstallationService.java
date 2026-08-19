package com.example.backend.service.admin;

import com.example.backend.dto.admin.Mission.MissionRequestDTO;
import com.example.backend.dto.admin.Mission.MissionResponseDTO;
import com.example.backend.entity.Administrateur;
import com.example.backend.entity.Etablissement;
import com.example.backend.entity.EquipeTechnique;
import com.example.backend.entity.Intervention;
import com.example.backend.entity.MissionInstallation;
import com.example.backend.entity.Utilisateur;
import com.example.backend.repository.admin.EtablissementRepository;
import com.example.backend.repository.admin.InterventionRepository;
import com.example.backend.repository.admin.MissionInstallationRepository;
import com.example.backend.repository.admin.EquipeTechniqueRepository;
import com.example.backend.repository.admin.RapportRepository;
import com.example.backend.repository.admin.PhotoRepository;
import com.example.backend.repository.admin.AttestationRepository;
import com.example.backend.repository.admin.ChecklistEquipementRepository;
import com.example.backend.repository.UtilisateurRepository;
import com.example.backend.service.NotificationHelperService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

        if (dto.getStatut() == null || dto.getStatut().isEmpty()) {
            dto.setStatut("Planifiée");
        }

        MissionResponseDTO createdMission = createMission(dto);

        String message = "Le technicien " + technicien.getPrenom() + " " + technicien.getNom() +
                " a créé une nouvelle mission : \"" + dto.getTitre() + "\" pour son équipe (" + equipe.getNomEquipe() + ").";

        notificationHelperService.notifierTousLesAdmins(technicien, message, "MISSION_CREEE");

        return createdMission;
    }

    @Transactional
    public MissionResponseDTO updateMission(Integer id, MissionRequestDTO dto) {
        MissionInstallation mission = missionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mission introuvable avec l'ID : " + id));

        mapDtoToEntity(dto, mission);

        MissionInstallation updated = missionRepository.save(mission);
        return new MissionResponseDTO(updated);
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

        boolean auMoinsUneEnCours = interventions.stream()
                .anyMatch(i -> "En cours".equals(i.getStatut()));

        if (toutesCloturees) {
            mission.setStatut("Exécutée");
        } else if (auMoinsUneEnCours) {
            mission.setStatut("En cours");
        }

        missionRepository.save(mission);
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
        } else {
            mission.setAdministrateur(null);
        }

        // Gestion Équipe : on ne modifie que si une nouvelle équipe est fournie
        if (dto.getIdEquipe() != null) {
            EquipeTechnique equipe = equipeTechniqueRepository.findById(dto.getIdEquipe())
                    .orElseThrow(() -> new RuntimeException("Équipe technique introuvable avec l'ID : " + dto.getIdEquipe()));
            mission.setEquipe(equipe);
        } else if (mission.getIdMission() == null) {
            // Si création et pas d'équipe, on met null
            mission.setEquipe(null);
        }
        // Si modification et dto.getIdEquipe() est null, on ne fait rien (on garde l'équipe actuelle)
    }
}