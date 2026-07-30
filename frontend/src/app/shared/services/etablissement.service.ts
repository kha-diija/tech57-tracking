import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Etablissement, EtablissementKpi, EtablissementRequest } from '../models/etablissement.model';

@Injectable({ providedIn: 'root' })
export class EtablissementService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/admin/etablissements`;

  getAll(): Observable<Etablissement[]> {
    return this.http.get<Etablissement[]>(this.apiUrl);
  }

  getKpis(): Observable<EtablissementKpi> {
    return this.http.get<EtablissementKpi>(`${this.apiUrl}/kpis`);
  }

  getById(id: number): Observable<Etablissement> {
    return this.http.get<Etablissement>(`${this.apiUrl}/${id}`);
  }

  create(payload: EtablissementRequest): Observable<Etablissement> {
    return this.http.post<Etablissement>(this.apiUrl, payload);
  }

  update(id: number, payload: EtablissementRequest): Observable<Etablissement> {
    return this.http.put<Etablissement>(`${this.apiUrl}/${id}`, payload);
  }

  delete(id: number, force: boolean = false): Observable<void> {
    let params = new HttpParams();
    if (force) {
      params = params.set('force', 'true');
    }
    return this.http.delete<void>(`${this.apiUrl}/${id}`, { params });
  }
}