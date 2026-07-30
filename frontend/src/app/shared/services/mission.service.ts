import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { delay } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { Mission, MissionKpi, MissionPayload } from '../models/mission.model';

@Injectable({ providedIn: 'root' })
export class MissionService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/admin/missions`;

  // MOCK temporaire pour le frontend
  private readonly mockMissions: Mission[] = [
    {
      idMission: 1,
      reference: 'MSN-2026-001',
      titre: 'Installation équipement multimédia et routeur',
      etablissementNom: 'Lycée Ibn Sina',
      equipeAffectee: 'Équipe A (Ahmed & Karim)',
      datePrevue: '2026-07-20',
      priorite: 'HAUTE',
      statut: 'EN_COURS',
      budgetEstime: 1500
    },
    {
      idMission: 2,
      reference: 'MSN-2026-002',
      titre: 'Déploiement Kits VEX GO et Datashow',
      etablissementNom: 'École Al Farabi',
      equipeAffectee: 'Équipe B (Sara & Omar)',
      datePrevue: '2026-07-25',
      priorite: 'NORMALE',
      statut: 'VALIDEE',
      budgetEstime: 1200
    }
  ];

  getAll(): Observable<Mission[]> {
    return of(this.mockMissions).pipe(delay(250));
    // Plus tard : return this.http.get<Mission[]>(this.apiUrl);
  }

  getKpis(): Observable<MissionKpi> {
    return of({
      totalMissions: this.mockMissions.length,
      validees: this.mockMissions.filter(m => m.statut === 'VALIDEE').length,
      enCours: this.mockMissions.filter(m => m.statut === 'EN_COURS').length,
      terminees: this.mockMissions.filter(m => m.statut === 'TERMINEE').length
    }).pipe(delay(200));
  }

  create(payload: MissionPayload): Observable<Mission> {
    const newItem: Mission = { ...payload, idMission: Date.now() };
    this.mockMissions.unshift(newItem);
    return of(newItem).pipe(delay(200));
  }

  delete(id: number): Observable<void> {
    const index = this.mockMissions.findIndex(m => m.idMission === id);
    if (index !== -1) this.mockMissions.splice(index, 1);
    return of(undefined).pipe(delay(200));
  }
}