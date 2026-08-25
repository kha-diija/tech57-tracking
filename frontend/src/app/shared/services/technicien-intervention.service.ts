import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Intervention } from '../models/intervention.model';

@Injectable({
  providedIn: 'root'
})
export class TechnicienInterventionService {
  private http = inject(HttpClient);
  private apiUrl = 'http://localhost:8080/api/technicien/interventions';

  getMesInterventions(): Observable<Intervention[]> {
    return this.http.get<Intervention[]>(this.apiUrl);
  }

  getInterventionById(id: number): Observable<Intervention> {
    return this.http.get<Intervention>(`${this.apiUrl}/${id}`);
  }

  createIntervention(data: any): Observable<Intervention> {
    return this.http.post<Intervention>(this.apiUrl, data);
  }

  checkIn(id: number, gpsCheckin: string): Observable<Intervention> {
    return this.http.post<Intervention>(`${this.apiUrl}/${id}/check-in`, { gpsCheckin });
  }

  checkOut(id: number, data: any, photos: { file: File, type: string }[], attestationFile: File | null): Observable<Intervention> {
    const formData = new FormData();

    formData.append('gpsCheckout', data.gpsCheckout || '');
    
    // ✅ AJOUT : Bénéficiaires réel
    if (data.beneficiairesReel) {
      formData.append('beneficiairesReel', String(data.beneficiairesReel));
    }

    (data.materielSortiIds || []).forEach((v: number) => formData.append('materielSortiIds', String(v)));
    (data.materielRetourIds || []).forEach((v: number) => formData.append('materielRetourIds', String(v)));
    (data.etatsRetours || []).forEach((v: string) => formData.append('etatsRetours', v));

    formData.append('checklist', JSON.stringify(data.checklist || []));

    photos.forEach(p => {
      formData.append('photos', p.file);
      formData.append('photoTypes', p.type);
    });

    if (attestationFile) {
      formData.append('attestationFile', attestationFile);
    }

    return this.http.post<Intervention>(`${this.apiUrl}/${id}/check-out`, formData);
  }

  genererRapport(id: number): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/${id}/rapport/download`, { responseType: 'blob' });
  }

  downloadAttestation(id: number): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/${id}/attestation/download`, { responseType: 'blob' });
  }

  updateIntervention(id: number, data: any): Observable<Intervention> {
    return this.http.put<Intervention>(`${this.apiUrl}/${id}`, data);
  }
}