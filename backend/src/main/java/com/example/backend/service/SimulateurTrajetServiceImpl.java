package com.example.backend.service;

import com.example.backend.dto.OsrmResponse;
import com.example.backend.dto.SimulateurTrajetDTO;
import com.example.backend.dto.SimulateurTrajetRequest;
import com.example.backend.dto.SimulateurTrajetRequest.PointRequest;
import com.example.backend.entity.*;
import com.example.backend.exception.BusinessException;
import com.example.backend.repository.adminEtablissementRepository;
import com.example.backend.repository.adminMissionInstallationRepository;
import com.example.backend.repository.SimulateurTrajetRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Calcule un itinéraire réel (type Waze) entre deux points via OSRM.
 * Chaque point (origine/destination) peut être SOIT un établissement existant,
 * SOIT un lieu libre choisi via la recherche d'adresse (Nominatim côté frontend) —
 * on n'est plus limité aux établissements enregistrés en base.
 */
@Service
public class SimulateurTrajetServiceImpl implements SimulateurTrajetService {

    private static final Logger log = LoggerFactory.getLogger(SimulateurTrajetServiceImpl.class);

    private final adminEtablissementRepository adminEtablissementRepository;
    private final SimulateurTrajetRepository simulateurTrajetRepository;
    private final adminMissionInstallationRepository adminMissionInstallationRepository;
    private final RestClient restClient;

    @Value("${app.osrm.base-url:https://router.project-osrm.org}")
    private String osrmBaseUrl;

    @Value("${app.trajet.consommation-defaut-l100km:7.5}")
    private double consommationDefaut;

    @Value("${app.trajet.tarif-peage-autoroute-dh-km:0.90}")
    private double tarifPeageParKm;

    @Value("${app.trajet.majoration-nationale-temps:1.25}")
    private double majorationTempsNationale;

    public SimulateurTrajetServiceImpl(adminEtablissementRepository adminEtablissementRepository,
                                       SimulateurTrajetRepository simulateurTrajetRepository,
                                       adminMissionInstallationRepository adminMissionInstallationRepository) {
        this.adminEtablissementRepository = adminEtablissementRepository;
        this.simulateurTrajetRepository = simulateurTrajetRepository;
        this.adminMissionInstallationRepository = adminMissionInstallationRepository;
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
            MissionInstallation mission = adminMissionInstallationRepository.findById(request.getIdMission())
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
        MissionInstallation mission = adminMissionInstallationRepository.findById(idMission)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mission introuvable"));

        mission.setBudgetPropose(simulation.getCoutTotal());
        adminMissionInstallationRepository.save(mission);

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

    private PointResolu resoudrePoint(PointRequest point) {
        if (!point.estLibre()) {
            Etablissement etab = adminEtablissementRepository.findById(point.getIdEtablissement())
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
            if (s.getEtablissementOrigine() != null) {
                Etablissement e = s.getEtablissementOrigine();
                double[] coords = parseCoordonnees(e);
                return new PointResolu(e, e.getDesignation(), coords[0], coords[1]);
            } else {
                return new PointResolu(null, s.getNomOrigine(), s.getLatOrigine(), s.getLngOrigine());
            }
        } else {
            if (s.getEtablissementDestination() != null) {
                Etablissement e = s.getEtablissementDestination();
                double[] coords = parseCoordonnees(e);
                return new PointResolu(e, e.getDesignation(), coords[0], coords[1]);
            } else {
                return new PointResolu(null, s.getNomDestination(), s.getLatDestination(), s.getLngDestination());
            }
        }
    }

    private double[] parseCoordonnees(Etablissement etablissement) {
        String gps = etablissement.getLocalisationGps();
        if (gps == null || !gps.contains(",")) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Coordonnées GPS manquantes pour l'établissement " + etablissement.getReference());
        }
        String[] parts = gps.split(",");
        return new double[]{Double.parseDouble(parts[0].trim()), Double.parseDouble(parts[1].trim())};
    }

    // ------------------------------------------------------------------
    // Appel OSRM avec validation et mapping
    // ------------------------------------------------------------------

    private RouteOsrm appellerOsrm(PointResolu origine, PointResolu destination) {
        try {
            // OSRM requiert le format : /route/v1/driving/{lon1},{lat1};{lon2},{lat2}
            // Force le formatage US pour éviter les virgules comme séparateurs décimaux (ex: 33,88 -> 33.88)
            String coordinates = String.format(java.util.Locale.US, "%.6f,%.6f;%.6f,%.6f",
                    origine.lng, origine.lat,
                    destination.lng, destination.lat);

            String url = UriComponentsBuilder.fromHttpUrl(osrmBaseUrl)
                    .path("/route/v1/driving/")
                    .path(coordinates)
                    .queryParam("overview", "false")
                    .toUriString();

            log.info("Appel OSRM URL: {}", url);

            OsrmResponse response = restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(OsrmResponse.class);

            if (response == null || response.getRoutes() == null || response.getRoutes().isEmpty()) {
                throw new BusinessException("Aucun itinéraire trouvé par OSRM.");
            }

            OsrmResponse.Route route = response.getRoutes().get(0);

            List<double[]> routeCoordinates = null;
            if (route.getGeometry() != null && route.getGeometry().getCoordinates() != null) {
                routeCoordinates = route.getGeometry().getCoordinates().stream()
                        .map(point -> new double[]{ point.get(1), point.get(0) }) // [lat, lng]
                        .toList();
            }

            return new RouteOsrm(route.getDistance(), route.getDuration(), routeCoordinates);

        } catch (HttpClientErrorException e) {
            log.error("Erreur OSRM (HTTP {}): {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new BusinessException("Le service de routage est temporairement indisponible ou la requête est invalide. Vérifiez les coordonnées.");
        } catch (Exception e) {
            log.error("Erreur lors de l'appel OSRM", e);
            throw new BusinessException("Impossible de calculer l'itinéraire.");
        }
    }

    /**
     * Convertit la réponse OSRM en objet métier RouteOsrm.
     * Inverse les coordonnées GeoJSON ([lng, lat]) → [lat, lng] pour Leaflet.
     */
    private RouteOsrm mapperOsrm(OsrmResponse response) {
        OsrmResponse.Route route = response.getRoutes().get(0);
        List<double[]> points = route.getGeometry().getCoordinates().stream()
                .map(coord -> new double[]{coord.get(1), coord.get(0)}) // inversion
                .collect(Collectors.toList());

        return new RouteOsrm(route.getDistance(), route.getDuration(), points);
    }

    private void validerCoordonnees(double lat, double lng, String nomPoint) {
        if (Double.isNaN(lat) || Double.isInfinite(lat) || Double.isNaN(lng) || Double.isInfinite(lng)) {
            throw new BusinessException("Coordonnées invalides (NaN ou Infini) pour " + nomPoint);
        }
        if (lat < -90 || lat > 90 || lng < -180 || lng > 180) {
            throw new BusinessException("Coordonnées hors plage pour " + nomPoint + " : lat=" + lat + ", lng=" + lng);
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

    // ------------------------------------------------------------------
    // Records internes
    // ------------------------------------------------------------------

    private record PointResolu(Etablissement etablissement, String nom, double lat, double lng) {}

    private record RouteOsrm(double distanceMetres, double dureeSecondes, List<double[]> geometrie) {}
}