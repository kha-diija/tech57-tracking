import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Commune } from '../models/etablissement.model';

@Injectable({ providedIn: 'root' })
export class CommuneService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/communes`;

  getAll(): Observable<Commune[]> {
    return this.http.get<Commune[]>(this.apiUrl);
  }
}