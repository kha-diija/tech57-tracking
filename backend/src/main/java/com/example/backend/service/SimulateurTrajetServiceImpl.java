package com.example.backend.service;

import com.example.backend.dto.SimulateurTrajetDTO;
import com.example.backend.dto.SimulateurTrajetRequest;
import com.example.backend.dto.SimulateurTrajetRequest.PointRequest;
import com.example.backend.entity.*;
import com.example.backend.repository.EtablissementRepository;
import com.example.backend.repository.MissionInstallationRepository;
import com.example.backend.repository.SimulateurTrajetRepository;
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
 * Calcule un itinéraire réel (type Waze) entre deux points via OSRM.
 * Chaque point (origine/destination) peut être SOIT un établissement existant,
 * SOIT un lieu libre choisi via la recherche d'adresse (Nominatim côté frontend) —
 * on n'est plus limité aux établissements enregistrés en base.
 */
@Service
public class SimulateurTrajetServiceImpl implements SimulateurTrajetService {

    private final EtablissementRepository etablissementRepository;
    private final SimulateurTrajetRepository simulateurTrajetRepository;
    private final MissionInstallationRepository missionInstallationRepository;
    private final RestClient restClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${app.osrm.base-url:https://router.project-osrm.org}")
    private String osrmBaseUrl;

    @Value("${app.trajet.consommation-defaut-l100km:7.5}")
    private double consommationDefaut;

    @Value("${app.trajet.tarif-peage-autoroute-dh-km:0.90}")
    private double tarifPeageParKm;

    @Value("${app.trajet.majoration-nationale-temps:1.25}")
    private double majorationTempsNationale;

    public SimulateurTrajetServiceImpl(EtablissementRepository etablissementRepository,
                                       SimulateurTrajetRepository simulateurTrajetRepository,
                                       MissionInstallationRepository missionInstallationRepository) {
        this.etablissementRepository = etablissementRepository;
        this.simulateurTrajetRepository = simulateurTrajetRepository;
        this.missionInstallationRepository = missionInstallationRepository;
        this.restClient = RestClient.create();
    }

    @Override
    public SimulateurTrajetDTO calculer(SimulateurTrajetRequest request, Integer idUtilisateurConnecte, boolean estAdministrateur) {
        PointResolu origine = resoudrePoint(request.getOrigine());
        PointResolu destination = resoudrePoint(request.getDestination());

        if (origine.lat == destination.lat && origine.lng == destination.lng) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le départ et l'arrivée sont identiques.");
        }

        RouteOsrm route = appellerOsrm(origine, destination);

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

        // Origine : établissement existant OU point libre (nom + coordonnées)
        if (origine.etablissement != null) {
            entity.setEtablissementOrigine(origine.etablissement);
        } else {
            entity.setNomOrigine(origine.nom);
            entity.setLatOrigine(origine.lat);
            entity.setLngOrigine(origine.lng);
        }
        if (destination.etablissement != null) {
            entity.setEtablissementDestination(destination.etablissement);
        } else {
            entity.setNomDestination(destination.nom);
            entity.setLatDestination(destination.lat);
            entity.setLngDestination(destination.lng);
        }

        if (estAdministrateur) {
            Administrateur admin = new Administrateur();
            admin.setId(idUtilisateurConnecte);
            entity.setAdministrateur(admin);
        } else {
            Technicien tech = new Technicien();
            tech.setId(idUtilisateurConnecte);
            entity.setTechnicien(tech);
        }

        if (request.getIdMission() != null) {
            MissionInstallation mission = missionInstallationRepository.findById(request.getIdMission())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mission introuvable"));
            entity.setMission(mission);
        }

        entity = simulateurTrajetRepository.save(entity);

        return toDto(entity, origine, destination, route.geometrie);
    }

    @Override
    public List<SimulateurTrajetDTO> comparerItineraires(SimulateurTrajetRequest request, Integer idUtilisateurConnecte, boolean estAdministrateur) {
        List<SimulateurTrajetDTO> resultats = new ArrayList<>();
        for (String type : List.of("Autoroute", "Nationale")) {
            request.setTypeRoute(type);
            resultats.add(calculer(request, idUtilisateurConnecte, estAdministrateur));
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

        mission.setBudgetPropose(simulation.getCoutTotal());
        missionInstallationRepository.save(mission);

        simulation.setMission(mission);
        simulation = simulateurTrajetRepository.save(simulation);

        return toDto(simulation, versPointResolu(simulation, true), versPointResolu(simulation, false), null);
    }

    @Override
    public List<SimulateurTrajetDTO> historiqueParTechnicien(Integer idTechnicien) {
        return simulateurTrajetRepository.findByTechnicien_IdOrderByIdSimulationDesc(idTechnicien)
                .stream()
                .map(s -> toDto(s, versPointResolu(s, true), versPointResolu(s, false), null))
                .toList();
    }

    // ------------------------------------------------------------------
    // Helpers privés
    // ------------------------------------------------------------------

    /** Résout un PointRequest en établissement existant OU point libre validé. */
    private PointResolu resoudrePoint(PointRequest point) {
        if (!point.estLibre()) {
            Etablissement etab = etablissementRepository.findById(point.getIdEtablissement())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Établissement introuvable : " + point.getIdEtablissement()));
            double[] coords = parseCoordonnees(etab);
            return new PointResolu(etab, etab.getDesignation(), coords[0], coords[1]);
        }
        if (point.getLat() == null || point.getLng() == null || point.getNom() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Un point libre doit fournir nom, lat et lng (résultat de la recherche d'adresse).");
        }
        return new PointResolu(null, point.getNom(), point.getLat(), point.getLng());
    }

    private PointResolu versPointResolu(SimulateurTrajet s, boolean estOrigine) {
        if (estOrigine) {
            return s.getEtablissementOrigine() != null
                    ? new PointResolu(s.getEtablissementOrigine(), s.getEtablissementOrigine().getDesignation(),
                    parseCoordonnees(s.getEtablissementOrigine())[0], parseCoordonnees(s.getEtablissementOrigine())[1])
                    : new PointResolu(null, s.getNomOrigine(), s.getLatOrigine(), s.getLngOrigine());
        }
        return s.getEtablissementDestination() != null
                ? new PointResolu(s.getEtablissementDestination(), s.getEtablissementDestination().getDesignation(),
                parseCoordonnees(s.getEtablissementDestination())[0], parseCoordonnees(s.getEtablissementDestination())[1])
                : new PointResolu(null, s.getNomDestination(), s.getLatDestination(), s.getLngDestination());
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

    private RouteOsrm appellerOsrm(PointResolu origine, PointResolu destination) {
        // OSRM attend lng,lat (et non lat,lng)
        String url = String.format("%s/route/v1/driving/%f,%f;%f,%f?overview=full&geometries=geojson",
                osrmBaseUrl, origine.lng, origine.lat, destination.lng, destination.lat);
        try {
            String body = restClient.get().uri(url).retrieve().body(String.class);
            JsonNode root = objectMapper.readTree(body);
            JsonNode route0 = root.path("routes").get(0);
            double distanceMetres = route0.path("distance").asDouble();
            double dureeSecondes = route0.path("duration").asDouble();

            List<double[]> points = new ArrayList<>();
            JsonNode coords = route0.path("geometry").path("coordinates");
            for (JsonNode c : coords) {
                points.add(new double[] { c.get(1).asDouble(), c.get(0).asDouble() });
            }
            return new RouteOsrm(distanceMetres, dureeSecondes, points);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Impossible de calculer l'itinéraire (service de routage indisponible)", e);
        }
    }

    private double arrondir(double valeur) {
        return Math.round(valeur * 100.0) / 100.0;
    }

    private SimulateurTrajetDTO toDto(SimulateurTrajet entity, PointResolu origine, PointResolu destination, List<double[]> geometrie) {
        SimulateurTrajetDTO dto = new SimulateurTrajetDTO();
        dto.setIdSimulation(entity.getIdSimulation());
        dto.setReferenceOrigine(origine.etablissement != null ? origine.etablissement.getReference() : null);
        dto.setDesignationOrigine(origine.nom);
        dto.setLatOrigine(origine.lat);
        dto.setLngOrigine(origine.lng);
        dto.setReferenceDestination(destination.etablissement != null ? destination.etablissement.getReference() : null);
        dto.setDesignationDestination(destination.nom);
        dto.setLatDestination(destination.lat);
        dto.setLngDestination(destination.lng);
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

    private record PointResolu(Etablissement etablissement, String nom, double lat, double lng) {}

    private record RouteOsrm(double distanceMetres, double dureeSecondes, List<double[]> geometrie) {}
}