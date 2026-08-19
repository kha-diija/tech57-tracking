import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { MissionInstallation, MissionRequestDTO } from '../models/mission.model';

@Injectable({
  providedIn: 'root'
})
export class TechnicienMissionService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = 'http://localhost:8080/api/technicien/missions';

  // Récupérer uniquement les missions de l'équipe du technicien connecté
  getByTechnicien(idTechnicien: number): Observable<MissionInstallation[]> {
    return this.http.get<MissionInstallation[]>(this.apiUrl, {
      params: { idTechnicien: idTechnicien.toString() }
    });
  }

  // Créer une mission (affectée automatiquement à son équipe + notification temps réel admin)
  create(payload: MissionRequestDTO, idTechnicien: number): Observable<MissionInstallation> {
    return this.http.post<MissionInstallation>(this.apiUrl, payload, {
      params: { idTechnicien: idTechnicien.toString() }
    });
  }

  // Modifier une mission (pas de suppression autorisée)
  update(id: number, payload: MissionRequestDTO): Observable<MissionInstallation> {
    return this.http.put<MissionInstallation>(`${this.apiUrl}/${id}`, payload);
  }
}