import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../../environments/environment';
import { SortieARegulariserDto, ValiderRetourRequest } from '../models/retour.model';

@Injectable({ providedIn: 'root' })
export class RetourMaterielGestionService {
  private http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/gestionnaire-stock/retours`;

  listerARegulariser(): Observable<SortieARegulariserDto[]> {
    return this.http.get<SortieARegulariserDto[]>(`${this.base}/a-regulariser`);
  }

  validerRetour(idSortie: number, payload: ValiderRetourRequest): Observable<void> {
    return this.http.post<void>(`${this.base}/${idSortie}/valider`, payload);
  }
}