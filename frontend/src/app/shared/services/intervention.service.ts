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

  // --- MÉTHODE STANDARD (Peut être mise en cache par le navigateur) ---
  getInterventions(): Observable<Intervention[]> {
    return this.http.get<Intervention[]>(this.apiUrl);
  }

  // --- MÉTHODE ANTI-CACHE (Utilisée par loadInterventions) ---
  // Le paramètre '_t' avec un timestamp différent à chaque appel 
  // oblige le navigateur à ignorer le cache et à demander les vraies données fraîches au Backend.
  getInterventionsNoCache(): Observable<Intervention[]> {
    return this.http.get<Intervention[]>(`${this.apiUrl}?_t=${new Date().getTime()}`);
  }

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

  forceCompleteIntervention(id: number): Observable<Intervention> {
    return this.http.patch<Intervention>(`${this.apiUrl}/${id}/force-complete`, {});
  }
}