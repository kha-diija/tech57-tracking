import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { MissionInstallation, MissionRequestDTO } from '../models/mission.model';

@Injectable({
  providedIn: 'root'
})
export class MissionService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = 'http://localhost:8080/api/admin/missions';

  getAll(): Observable<MissionInstallation[]> {
    return this.http.get<MissionInstallation[]>(this.apiUrl);
  }

  getById(id: number): Observable<MissionInstallation> {
    return this.http.get<MissionInstallation>(`${this.apiUrl}/${id}`);
  }

  create(payload: MissionRequestDTO): Observable<MissionInstallation> {
    return this.http.post<MissionInstallation>(this.apiUrl, payload);
  }

  update(id: number, payload: MissionRequestDTO): Observable<MissionInstallation> {
    return this.http.put<MissionInstallation>(`${this.apiUrl}/${id}`, payload);
  }

  delete(id: number, force: boolean = false): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`, {
      params: { force: force.toString() }
    });
  }

  // ✅ NOUVELLE MÉTHODE : Approuver la mission
  approuver(id: number): Observable<MissionInstallation> {
    return this.http.put<MissionInstallation>(`${this.apiUrl}/${id}/approuver`, {});
  }

  // ✅ NOUVELLE MÉTHODE : Rejeter la mission
  rejeter(id: number, motif: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}/rejeter`, {
      params: { motif: motif }
    });
  }
}