package com.example.backend.service;

import com.example.backend.dto.SimulateurTrajetDTO;
import com.example.backend.dto.SimulateurTrajetRequest;
import com.example.backend.entity.Administrateur;
import com.example.backend.entity.Etablissement;
import com.example.backend.entity.MissionInstallation;
import com.example.backend.entity.SimulateurTrajet;
import com.example.backend.entity.Technicien;
import com.example.backend.repository.AdministrateurRepository;
import com.example.backend.repository.adminEtablissementRepository;
import com.example.backend.repository.adminMissionInstallationRepository;
import com.example.backend.repository.SimulateurTrajetRepository;
import com.example.backend.repository.adminTechnicienRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Calcule un itinéraire réel (type Waze) entre deux établissements via OSRM
 * (Open Source Routing Machine — pas de clé API requise) puis estime le coût
 * gasoil / péage à partir des tarifs configurés (application.yml).
 *
 * Pourquoi OSRM plutôt que Google Directions : gratuit, pas de quota, pas de
 * clé à gérer, largement suffisant pour un calcul routier point-à-point.
 * Le endpoint public (router.project-osrm.org) convient pour le développement ;
 * en production, préférer un conteneur OSRM auto-hébergé avec l'extrait
 * OSM du Maroc (cf. README, section "Déploiement OSRM").
 */
@Service
public class SimulateurTrajetServiceImpl implements SimulateurTrajetService {

    private final adminEtablissementRepository etablissementRepository;
    private final SimulateurTrajetRepository simulateurTrajetRepository;
    private final adminMissionInstallationRepository missionInstallationRepository;
    private final adminTechnicienRepository technicienRepository;
    private final AdministrateurRepository administrateurRepository;
    private final RestClient restClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${app.osrm.base-url:https://router.project-osrm.org}")
    private String osrmBaseUrl;

    @Value("${app.trajet.consommation-defaut-l100km:7.5}")
    private double consommationDefaut;

    @Value("${app.trajet.tarif-peage-autoroute-dh-km:0.90}")
    private double tarifPeageParKm;

    @Value("${app.trajet.majoration-nationale-temps:1.25}")
    private double majorationTempsNationale; // route nationale = plus lente qu'autoroute à distance égale

    public SimulateurTrajetServiceImpl(adminEtablissementRepository etablissementRepository,
                                       SimulateurTrajetRepository simulateurTrajetRepository,
                                       adminMissionInstallationRepository missionInstallationRepository,
                                       adminTechnicienRepository technicienRepository,
                                       AdministrateurRepository administrateurRepository) {
        this.etablissementRepository = etablissementRepository;
        this.simulateurTrajetRepository = simulateurTrajetRepository;
        this.missionInstallationRepository = missionInstallationRepository;
        this.technicienRepository = technicienRepository;
        this.administrateurRepository = administrateurRepository;
        this.restClient = RestClient.create();
    }

    @Override
    public SimulateurTrajetDTO calculer(SimulateurTrajetRequest request, Integer idUtilisateurConnecte, boolean estAdministrateur) {
        Etablissement origine = getEtablissement(request.getIdEtablissementOrigine());
        Etablissement destination = getEtablissement(request.getIdEtablissementDestination());

        double[] coordOrigine = parseCoordonnees(origine);
        double[] coordDestination = parseCoordonnees(destination);

        RouteOsrm route = appellerOsrm(coordOrigine, coordDestination);

        boolean estAutoroute = "Autoroute".equalsIgnoreCase(request.getTypeRoute());
        double consommation = request.getConsommationL100km() != null ? request.getConsommationL100km() : consommationDefaut;

        double distanceKm = arrondir(route.distanceMetres / 1000.0);
        double heures = route.dureeSecondes / 3600.0 * (estAutoroute ? 1.0 : majorationTempsNationale);
        double tempsEstime = arrondir(heures);

        double coutGasoil = arrondir(distanceKm * consommation / 100.0 * request.getPrixCarburantLitre());
        double coutPeage = estAutoroute ? arrondir(distanceKm * tarifPeageParKm) : 0.0;
        double coutTotal = arrondir(coutGasoil + coutPeage);

        SimulateurTrajet entity = new SimulateurTrajet();
        entity.setDistanceKm(distanceKm);
        entity.setTypeRoute(request.getTypeRoute());
        entity.setCoutGasoil(coutGasoil);
        entity.setCoutPeage(coutPeage);
        entity.setTempsEstime(tempsEstime);
        entity.setCoutTotal(coutTotal);
        entity.setEtablissementOrigine(origine);
        entity.setEtablissementDestination(destination);

        if (estAdministrateur) {
            Administrateur admin = administrateurRepository.findById(idUtilisateurConnecte)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Administrateur introuvable"));
            entity.setAdministrateur(admin);
        } else {
            Technicien technicien = technicienRepository.findById(idUtilisateurConnecte)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Technicien introuvable"));
            entity.setTechnicien(technicien);
        }

        if (request.getIdMission() != null) {
            MissionInstallation mission = missionInstallationRepository.findById(request.getIdMission())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mission introuvable"));
            entity.setMission(mission);
        }

        entity = simulateurTrajetRepository.save(entity);

        return toDto(entity, origine, destination, coordOrigine, coordDestination, route.geometrie);
    }

    @Override
    public List<SimulateurTrajetDTO> comparerItineraires(SimulateurTrajetRequest request, Integer idUtilisateurConnecte, boolean estAdministrateur) {
        List<SimulateurTrajetDTO> resultats = new ArrayList<>();
        for (String type : List.of("Autoroute", "Nationale")) {
            SimulateurTrajetRequest variante = copierAvecType(request, type);
            resultats.add(calculer(variante, idUtilisateurConnecte, estAdministrateur));
        }
        resultats.sort(Comparator.comparing(SimulateurTrajetDTO::getCoutTotal));
        return resultats;
    }

    @Override
    public SimulateurTrajetDTO proposerBudgetMission(Integer idSimulation, Integer idMission) {
        SimulateurTrajet simulation = simulateurTrajetRepository.findById(idSimulation)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Simulation introuvable"));
        MissionInstallation mission = missionInstallationRepository.findById(idMission)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mission introuvable"));

        mission.setBudgetPropose(simulation.getCoutTotal()); // Double -> Double, direct
        missionInstallationRepository.save(mission);

        simulation.setMission(mission);
        simulation = simulateurTrajetRepository.save(simulation);

        Etablissement origine = simulation.getEtablissementOrigine();
        Etablissement destination = simulation.getEtablissementDestination();
        return toDto(simulation, origine, destination, parseCoordonnees(origine), parseCoordonnees(destination), null);
    }

    @Override
    public List<SimulateurTrajetDTO> historiqueParTechnicien(Integer idTechnicien) {
        return simulateurTrajetRepository.findByTechnicien_IdOrderByIdSimulationDesc(idTechnicien)
                .stream()
                .map(s -> toDto(s, s.getEtablissementOrigine(), s.getEtablissementDestination(),
                        parseCoordonnees(s.getEtablissementOrigine()), parseCoordonnees(s.getEtablissementDestination()), null))
                .toList();
    }

    // ------------------------------------------------------------------
    // Helpers privés
    // ------------------------------------------------------------------

    private double arrondir(double valeur) {
        return Math.round(valeur * 100.0) / 100.0;
    }

    private Etablissement getEtablissement(Integer id) {
        return etablissementRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Établissement introuvable : " + id));
    }

    /** localisation_gps est stocké au format "lat,lng" (ex: "33.5731,-7.5898"). */
    private double[] parseCoordonnees(Etablissement etablissement) {
        String gps = etablissement.getLocalisationGps();
        if (gps == null || !gps.contains(",")) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Coordonnées GPS manquantes pour l'établissement " + etablissement.getReference());
        }
        String[] parts = gps.split(",");
        return new double[] { Double.parseDouble(parts[0].trim()), Double.parseDouble(parts[1].trim()) };
    }

    private RouteOsrm appellerOsrm(double[] origine, double[] destination) {
        // OSRM attend lng,lat (et non lat,lng)
        String url = String.format("%s/route/v1/driving/%f,%f;%f,%f?overview=full&geometries=geojson",
                osrmBaseUrl, origine[1], origine[0], destination[1], destination[0]);
        try {
            String body = restClient.get().uri(url).retrieve().body(String.class);
            JsonNode root = objectMapper.readTree(body);
            JsonNode route0 = root.path("routes").get(0);
            double distanceMetres = route0.path("distance").asDouble();
            double dureeSecondes = route0.path("duration").asDouble();

            List<double[]> points = new ArrayList<>();
            JsonNode coords = route0.path("geometry").path("coordinates");
            for (JsonNode c : coords) {
                // GeoJSON = [lng, lat] -> on repasse en [lat, lng] pour Leaflet
                points.add(new double[] { c.get(1).asDouble(), c.get(0).asDouble() });
            }
            return new RouteOsrm(distanceMetres, dureeSecondes, points);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Impossible de calculer l'itinéraire (service de routage indisponible)", e);
        }
    }

    private SimulateurTrajetRequest copierAvecType(SimulateurTrajetRequest source, String type) {
        SimulateurTrajetRequest copie = new SimulateurTrajetRequest();
        copie.setIdEtablissementOrigine(source.getIdEtablissementOrigine());
        copie.setIdEtablissementDestination(source.getIdEtablissementDestination());
        copie.setPrixCarburantLitre(source.getPrixCarburantLitre());
        copie.setConsommationL100km(source.getConsommationL100km());
        copie.setIdMission(source.getIdMission());
        copie.setTypeRoute(type);
        return copie;
    }

    private SimulateurTrajetDTO toDto(SimulateurTrajet entity, Etablissement origine, Etablissement destination,
                                       double[] coordOrigine, double[] coordDestination, List<double[]> geometrie) {
        SimulateurTrajetDTO dto = new SimulateurTrajetDTO();
        dto.setIdSimulation(entity.getIdSimulation());
        dto.setReferenceOrigine(origine.getReference());
        dto.setDesignationOrigine(origine.getDesignation());
        dto.setLatOrigine(coordOrigine[0]);
        dto.setLngOrigine(coordOrigine[1]);
        dto.setReferenceDestination(destination.getReference());
        dto.setDesignationDestination(destination.getDesignation());
        dto.setLatDestination(coordDestination[0]);
        dto.setLngDestination(coordDestination[1]);
        dto.setTypeRoute(entity.getTypeRoute());
        dto.setDistanceKm(entity.getDistanceKm());
        dto.setTempsEstime(entity.getTempsEstime());
        dto.setCoutGasoil(entity.getCoutGasoil());
        dto.setCoutPeage(entity.getCoutPeage());
        dto.setCoutTotal(entity.getCoutTotal());
        dto.setIdMission(entity.getMission() != null ? entity.getMission().getIdMission() : null);
        dto.setPointsRoute(geometrie);
        return dto;
    }

    private record RouteOsrm(double distanceMetres, double dureeSecondes, List<double[]> geometrie) {}
}