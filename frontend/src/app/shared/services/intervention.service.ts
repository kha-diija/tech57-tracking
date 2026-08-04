import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Intervention, InterventionRequest } from '../models/intervention.model';

@Injectable({
  providedIn: 'root'
})
export class InterventionService {
  private http = inject(HttpClient);
  private apiUrl = 'http://localhost:8080/api/admin/interventions';

  getInterventions(): Observable<Intervention[]> {
    return this.http.get<Intervention[]>(this.apiUrl);
  }

  // AJOUT DE CETTE MÉTHODE POUR RÉCUPÉRER LES DÉTAILS COMPLETS (PHOTOS, ATTESTATION)
  getInterventionById(id: number): Observable<Intervention> {
    return this.http.get<Intervention>(`${this.apiUrl}/${id}`);
  }

  createIntervention(data: InterventionRequest): Observable<Intervention> {
    return this.http.post<Intervention>(this.apiUrl, data);
  }

  updateIntervention(id: number, data: InterventionRequest): Observable<Intervention> {
    return this.http.put<Intervention>(`${this.apiUrl}/${id}`, data);
  }

  deleteIntervention(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}