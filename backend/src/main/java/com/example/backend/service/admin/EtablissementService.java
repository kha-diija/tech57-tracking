package com.example.backend.service.admin;

import com.example.backend.dto.admin.etablissement.*;
import com.example.backend.entity.Commune;
import com.example.backend.entity.Etablissement;
import com.example.backend.entity.Responsable;
import com.example.backend.repository.admin.*;
import com.example.backend.entity.Intervention;
import com.example.backend.entity.MissionInstallation;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.entity.Province;
import com.example.backend.repository.admin.ProvinceRepository;
import org.apache.poi.ss.usermodel.*;
import org.springframework.web.multipart.MultipartFile;

import com.example.backend.dto.admin.etablissement.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import static com.itextpdf.text.pdf.PdfName.FORMULA;
import static java.sql.Types.*;
import static javax.management.openmbean.SimpleType.STRING;

@Service
public class EtablissementService {

    private final EtablissementRepository etablissementRepository;
    private final CommuneRepository communeRepository;
    private final ResponsableRepository responsableRepository;
    private final MissionInstallationRepository missionInstallationRepository;
    private final InterventionRepository interventionRepository;
    private final RapportRepository rapportRepository;
    private final PhotoRepository photoRepository;
    private final AttestationRepository attestationRepository;
    private final ChecklistEquipementRepository checklistEquipementRepository;
    private final ChecklistItemRepository checklistItemRepository;// <-- Ajout de la dépendance

    private final ObservateurRepository observateurRepository;

    private final ProvinceRepository provinceRepository;

    public EtablissementService(EtablissementRepository etablissementRepository,
                                CommuneRepository communeRepository,
                                ResponsableRepository responsableRepository,
                                MissionInstallationRepository missionInstallationRepository,
                                InterventionRepository interventionRepository,
                                RapportRepository rapportRepository,
                                PhotoRepository photoRepository,
                                AttestationRepository attestationRepository,
                                ChecklistEquipementRepository checklistEquipementRepository,
                                ChecklistItemRepository checklistItemRepository,
                                ObservateurRepository observateurRepository,
                                ProvinceRepository provinceRepository) {
        this.etablissementRepository = etablissementRepository;
        this.communeRepository = communeRepository;
        this.responsableRepository = responsableRepository;
        this.missionInstallationRepository = missionInstallationRepository;
        this.interventionRepository = interventionRepository;
        this.rapportRepository = rapportRepository;
        this.photoRepository = photoRepository;
        this.attestationRepository = attestationRepository;
        this.checklistEquipementRepository = checklistEquipementRepository;
        this.checklistItemRepository = checklistItemRepository;
        this.observateurRepository = observateurRepository;
        this.provinceRepository = provinceRepository;
    }
    @Transactional(readOnly = true)
    public List<EtablissementResponse> getAll() {
        return etablissementRepository.findAllWithDetails()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public EtablissementResponse getById(Integer id) {
        Etablissement e = etablissementRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new NoSuchElementException("Établissement introuvable : " + id));
        return toResponse(e);
    }

    @Transactional(readOnly = true)
    public EtablissementKpiResponse getKpis() {
        EtablissementKpiResponse kpi = new EtablissementKpiResponse();
        List<Etablissement> all = etablissementRepository.findAllWithDetails();

        kpi.setTotalEtablissements(all.size());
        kpi.setRegionsCouvertes(etablissementRepository.countRegionsCouvertes());
        kpi.setTotalBeneficiaires(
                all.stream()
                        .mapToLong(e -> e.getNombreBeneficiaires() == null ? 0 : e.getNombreBeneficiaires())
                        .sum()
        );
        kpi.setSansResponsable(etablissementRepository.countSansResponsable());

        return kpi;
    }

    @Transactional
    public EtablissementResponse create(EtablissementRequest request) {
        Etablissement e = new Etablissement();
        applyRequest(e, request);
        Etablissement saved = etablissementRepository.save(e);
        return toResponse(etablissementRepository.findByIdWithDetails(saved.getIdEtablissement()).orElseThrow());
    }

    @Transactional
    public EtablissementResponse update(Integer id, EtablissementRequest request) {
        Etablissement e = etablissementRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Établissement introuvable : " + id));
        applyRequest(e, request);
        Etablissement saved = etablissementRepository.save(e);
        return toResponse(etablissementRepository.findByIdWithDetails(saved.getIdEtablissement()).orElseThrow());
    }


    /**
     * Suppression sécurisée avec gestion de la cascade et avertissement préalable.
     */
    @Transactional
    public void delete(Integer id, boolean force) {
        Etablissement e = etablissementRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Établissement introuvable : " + id));

        List<MissionInstallation> missions = missionInstallationRepository.findByEtablissementIdEtablissement(id);

        List<Intervention> interventions = missions.stream()
                .flatMap(m -> interventionRepository.findByMissionIdMission(m.getIdMission()).stream())
                .collect(Collectors.toList());

        long nbMateriels = etablissementRepository.countMaterielsByEtablissementId(id);
        long nbMissions = missions.size();
        long nbInterventions = interventions.size();

        if ((nbMateriels > 0 || nbMissions > 0 || nbInterventions > 0) && !force) {
            throw new IllegalStateException("Attention : Cet établissement contient " + nbMateriels +
                    " matériel(s), " + nbMissions + " mission(s) d'installation et " + nbInterventions +
                    " intervention(s) associée(s). Tout ce qui est lié à cet établissement va être supprimé.");
        }

        if (force) {
            for (Intervention intervention : interventions) {
                rapportRepository.deleteByIntervention(intervention);
                photoRepository.deleteByIntervention(intervention);
                attestationRepository.deleteByIntervention(intervention);
                checklistEquipementRepository.deleteByIntervention(intervention);
            }

            for (MissionInstallation mission : missions) {
                rapportRepository.deleteByMission(mission);
            }

            interventionRepository.deleteAll(interventions);
            missionInstallationRepository.deleteAll(missions);
        }

        etablissementRepository.delete(e);
    }

    @Transactional
    public EtablissementImportResult importFromExcel(MultipartFile file, Integer idProvince) {
        EtablissementImportResult result = new EtablissementImportResult();

        Province province = provinceRepository.findById(idProvince)
                .orElseThrow(() -> new NoSuchElementException("Province introuvable : " + idProvince));

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            int headerRowIndex = -1;
            Map<String, Integer> colIndex = new HashMap<>();

            for (int r = 0; r <= Math.min(10, sheet.getLastRowNum()); r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;
                for (Cell cell : row) {
                    if ("CD_ETAB".equalsIgnoreCase(getCellValueAsString(cell).trim())) {
                        headerRowIndex = r;
                        break;
                    }
                }
                if (headerRowIndex != -1) break;
            }

            if (headerRowIndex == -1) {
                throw new IllegalStateException("Colonne CD_ETAB introuvable dans le fichier (en-tête non reconnu).");
            }

            Row headerRow = sheet.getRow(headerRowIndex);
            for (Cell cell : headerRow) {
                String val = getCellValueAsString(cell).trim();
                if (!val.isEmpty()) {
                    colIndex.put(val.toUpperCase(), cell.getColumnIndex());
                }
            }

            int colCommune = colIndex.getOrDefault("LL_COM", -1);
            int colRef = colIndex.getOrDefault("CD_ETAB", -1);
            int colNom = colIndex.getOrDefault("NOM_ETABA", -1);
            int colType = colIndex.getOrDefault("LL_NETAB", -1);
            int colX = colIndex.getOrDefault("X", -1);
            int colY = colIndex.getOrDefault("Y", -1);
            int colEffectifAmi = colIndex.getOrDefault("NBRE D'ÉLÈVES SELON AMI", -1);
            int colEffectifRealise = colIndex.getOrDefault("NBRE D'ÉLÉVES RÉALISÉS", -1);

            for (int r = headerRowIndex + 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;

                String reference = colRef >= 0 ? getCellValueAsString(row.getCell(colRef)).trim() : "";
                if (reference.isEmpty()) continue; // ligne vide, on saute sans erreur

                result.setTotalLignes(result.getTotalLignes() + 1);

                try {
                    String communeNom = colCommune >= 0 ? getCellValueAsString(row.getCell(colCommune)).trim() : "";
                    String designation = colNom >= 0 ? getCellValueAsString(row.getCell(colNom)).trim() : "";
                    String type = colType >= 0 ? getCellValueAsString(row.getCell(colType)).trim() : "";
                    String x = colX >= 0 ? getCellValueAsString(row.getCell(colX)).trim() : "";
                    String y = colY >= 0 ? getCellValueAsString(row.getCell(colY)).trim() : "";

                    Integer effectif = colEffectifAmi >= 0 ? getCellValueAsInteger(row.getCell(colEffectifAmi)) : null;
                    if (effectif == null && colEffectifRealise >= 0) {
                        effectif = getCellValueAsInteger(row.getCell(colEffectifRealise));
                    }

                    Commune commune = resolveCommune(communeNom, province);

                    Etablissement e = etablissementRepository.findByReference(reference).orElse(null);
                    boolean isNew = (e == null);
                    if (isNew) {
                        e = new Etablissement();
                        e.setReference(reference);
                    }

                    e.setDesignation(designation.isEmpty() ? reference : designation);
                    e.setType(type.isEmpty() ? "Non précisé" : type);
                    if (!y.isEmpty() && !x.isEmpty()) {
                        e.setLocalisationGps(y + "," + x);
                    }
                    if (effectif != null) {
                        e.setNombreBeneficiaires(effectif);
                    }
                    e.setCommune(commune);

                    etablissementRepository.save(e);

                    if (isNew) {
                        result.setCrees(result.getCrees() + 1);
                    } else {
                        result.setMisAJour(result.getMisAJour() + 1);
                    }
                } catch (Exception rowEx) {
                    result.setIgnores(result.getIgnores() + 1);
                    result.getErreurs().add("Ligne " + (r + 1) + " (" + reference + ") : " + rowEx.getMessage());
                }
            }

        } catch (IOException ex) {
            throw new IllegalStateException("Impossible de lire le fichier Excel : " + ex.getMessage());
        }

        return result;
    }

    private Commune resolveCommune(String nom, Province province) {
        if (nom == null || nom.isBlank()) {
            throw new IllegalArgumentException("Commune manquante");
        }
        return communeRepository.findByNomIgnoreCaseAndProvince_IdProvince(nom, province.getIdProvince())
                .orElseGet(() -> {
                    Commune c = new Commune();
                    c.setNom(nom);
                    c.setCode("AUTO-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
                    c.setProvince(province);
                    return communeRepository.save(c);
                });
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING: return cell.getStringCellValue();
            case NUMERIC:
                double d = cell.getNumericCellValue();
                return (d == Math.floor(d)) ? String.valueOf((long) d) : String.valueOf(d);
            case BOOLEAN: return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try { return cell.getStringCellValue(); }
                catch (Exception e) {
                    try { return String.valueOf(cell.getNumericCellValue()); }
                    catch (Exception e2) { return ""; }
                }
            default: return "";
        }
    }

    private Integer getCellValueAsInteger(Cell cell) {
        String s = getCellValueAsString(cell).trim();
        if (s.isEmpty()) return null;
        try { return (int) Double.parseDouble(s); }
        catch (NumberFormatException e) { return null; }
    }
    // ============================================================
    // --- Mapping helpers ---
    // ============================================================

    private void applyRequest(Etablissement e, EtablissementRequest request) {
        e.setReference(request.getReference());
        e.setDesignation(request.getDesignation());
        e.setType(request.getType());
        e.setLocalisationGps(request.getLocalisationGps());
        e.setNombreBeneficiaires(request.getNombreBeneficiaires());
        e.setTelephoneContact(request.getTelephoneContact());
        e.setEmailContact(request.getEmailContact());

        Commune commune = communeRepository.findById(request.getIdCommune())
                .orElseThrow(() -> new NoSuchElementException("Commune introuvable : " + request.getIdCommune()));
        e.setCommune(commune);

        e.setResponsable(resolveResponsable(request.getResponsable()));
    }

    private Responsable resolveResponsable(ResponsableDto dto) {
        if (dto == null || isBlank(dto.getNom())) {
            return null;
        }

        Responsable responsable;
        if (dto.getIdResponsable() != null) {
            responsable = responsableRepository.findById(dto.getIdResponsable())
                    .orElseGet(Responsable::new);
        } else {
            responsable = new Responsable();
        }

        responsable.setNom(dto.getNom());
        responsable.setPrenom(dto.getPrenom());
        responsable.setFonction(dto.getFonction());
        responsable.setTelephone(dto.getTelephone());
        responsable.setEmail(dto.getEmail());

        return responsableRepository.save(responsable);
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private EtablissementResponse toResponse(Etablissement e) {
        EtablissementResponse r = new EtablissementResponse();
        r.setIdEtablissement(e.getIdEtablissement());
        r.setReference(e.getReference());
        r.setDesignation(e.getDesignation());
        r.setType(e.getType());
        r.setLocalisationGps(e.getLocalisationGps());
        r.setNombreBeneficiaires(e.getNombreBeneficiaires());
        r.setTelephoneContact(e.getTelephoneContact());
        r.setEmailContact(e.getEmailContact());

        if (e.getCommune() != null) {
            r.setIdCommune(e.getCommune().getIdCommune());
            r.setCommuneNom(e.getCommune().getNom());

            if (e.getCommune().getProvince() != null) {
                r.setIdProvince(e.getCommune().getProvince().getIdProvince());
                r.setProvinceNom(e.getCommune().getProvince().getNom());

                if (e.getCommune().getProvince().getRegion() != null) {
                    r.setIdRegion(e.getCommune().getProvince().getRegion().getIdRegion());
                    r.setRegionNom(e.getCommune().getProvince().getRegion().getNom());
                }
            }
        }

        if (e.getResponsable() != null) {
            ResponsableDto rd = new ResponsableDto();
            rd.setIdResponsable(e.getResponsable().getIdResponsable());
            rd.setNom(e.getResponsable().getNom());
            rd.setPrenom(e.getResponsable().getPrenom());
            rd.setFonction(e.getResponsable().getPrenom()); // S'assurer que les getters/setters correspondent
            rd.setTelephone(e.getResponsable().getTelephone());
            rd.setEmail(e.getResponsable().getEmail());
            r.setResponsable(rd);
        }
        r.setNbFormateurs((int) observateurRepository.countByEtablissement_IdEtablissement(e.getIdEtablissement()));

        return r;
    }
}