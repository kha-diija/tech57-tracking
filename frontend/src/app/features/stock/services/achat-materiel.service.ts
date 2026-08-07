import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { AchatMaterielDto, CreerAchatRequest } from '../models/achat.model';

@Injectable({ providedIn: 'root' })
export class AchatMaterielService {
  private http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/achats-materiel`;

  lister(): Observable<AchatMaterielDto[]> {
    return this.http.get<AchatMaterielDto[]>(this.base);
  }

  listerParMateriel(idMateriel: number): Observable<AchatMaterielDto[]> {
    return this.http.get<AchatMaterielDto[]>(`${this.base}/materiel/${idMateriel}`);
  }

  creer(payload: CreerAchatRequest): Observable<AchatMaterielDto> {
    return this.http.post<AchatMaterielDto>(this.base, payload);
  }
}