import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../../environments/environment';
import { RejeterSortieRequest, SortieMaterielDto } from '../models/sortie.model';

@Injectable({ providedIn: 'root' })
export class SortieMaterielGestionService {
  private http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/gestionnaire-stock/sorties`;

  lister(statut: string): Observable<SortieMaterielDto[]> {
    return this.http.get<SortieMaterielDto[]>(this.base, { params: { statut } });
  }

  approuver(idSortie: number): Observable<SortieMaterielDto> {
    return this.http.post<SortieMaterielDto>(`${this.base}/${idSortie}/approuver`, {});
  }

  rejeter(idSortie: number, payload: RejeterSortieRequest): Observable<SortieMaterielDto> {
    return this.http.post<SortieMaterielDto>(`${this.base}/${idSortie}/rejeter`, payload);
  }
}