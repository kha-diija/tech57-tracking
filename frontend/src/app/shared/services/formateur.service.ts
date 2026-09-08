import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Formateur, FormateurRequest } from '../models/etablissement.model';

@Injectable({ providedIn: 'root' })
export class FormateurService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/etablissements`;

  getByEtablissement(idEtablissement: number): Observable<Formateur[]> {
    return this.http.get<Formateur[]>(`${this.baseUrl}/${idEtablissement}/formateurs`);
  }

  create(idEtablissement: number, payload: FormateurRequest): Observable<Formateur> {
    return this.http.post<Formateur>(`${this.baseUrl}/${idEtablissement}/formateurs`, payload);
  }

  update(idEtablissement: number, idFormateur: number, payload: FormateurRequest): Observable<Formateur> {
    return this.http.put<Formateur>(`${this.baseUrl}/${idEtablissement}/formateurs/${idFormateur}`, payload);
  }

  delete(idEtablissement: number, idFormateur: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${idEtablissement}/formateurs/${idFormateur}`);
  }
}