import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface EtablissementOption {
  idEtablissement: number;
  reference: string;
  designation: string;
  localisationGps: string; // "lat,lng"
}

export type TypeRoute = 'Autoroute' | 'Nationale';

/**
 * Un point de trajet (origine ou destination), qui peut provenir soit
 * d'un établissement existant (idEtablissement renseigné), soit d'une
 * adresse libre trouvée via la recherche (idEtablissement = null).
 */
export interface PointTrajet {
  idEtablissement: number | null;
  nom: string;
  lat: number;
  lng: number;
}

export interface SimulateurTrajetRequest {
  origine: PointTrajet;
  destination: PointTrajet;
  typeRoute: TypeRoute;
  prixCarburantLitre: number;
  consommationL100km?: number;
  idMission?: number;
}

export interface SimulateurTrajetResult {
  idSimulation: number;
  referenceOrigine: string;
  designationOrigine: string;
  latOrigine: number;
  lngOrigine: number;
  referenceDestination: string;
  designationDestination: string;
  latDestination: number;
  lngDestination: number;
  typeRoute: TypeRoute;
  distanceKm: number;
  tempsEstime: number;
  coutGasoil: number;
  coutPeage: number;
  coutTotal: number;
  idMission: number | null;
  pointsRoute: [number, number][] | null;
}

@Injectable({ providedIn: 'root' })
export class SimulateurTrajetService {
  private readonly baseUrl = `${environment.apiUrl}/simulateur-trajet`;

  constructor(private http: HttpClient) {}

  calculer(request: SimulateurTrajetRequest): Observable<SimulateurTrajetResult> {
    return this.http.post<SimulateurTrajetResult>(`${this.baseUrl}/calculer`, request);
  }

  comparerItineraires(request: SimulateurTrajetRequest): Observable<SimulateurTrajetResult[]> {
    return this.http.post<SimulateurTrajetResult[]>(`${this.baseUrl}/comparer`, request);
  }

  proposerBudget(idSimulation: number, idMission: number): Observable<SimulateurTrajetResult> {
    return this.http.post<SimulateurTrajetResult>(
      `${this.baseUrl}/${idSimulation}/proposer-budget/${idMission}`,
      {}
    );
  }

  historique(idTechnicien: number): Observable<SimulateurTrajetResult[]> {
    return this.http.get<SimulateurTrajetResult[]>(`${this.baseUrl}/historique/${idTechnicien}`);
  }
}