import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { PartenaireDashboard } from '../models/partenaire.model';

@Injectable({ providedIn: 'root' })
export class PartenaireService {
  private readonly apiUrl = `${environment.apiUrl}/partenaire`;

  constructor(private http: HttpClient) {}

  getDashboard(): Observable<PartenaireDashboard> {
    return this.http.get<PartenaireDashboard>(`${this.apiUrl}/dashboard`);
  }
}