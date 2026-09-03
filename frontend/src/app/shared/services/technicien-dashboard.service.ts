import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { TechnicienKpiResponse } from '../models/technicien-dashboard.model';

@Injectable({
  providedIn: 'root'
})
export class TechnicienDashboardService {

  private apiUrl = 'http://localhost:8080/api/technicien/dashboard/kpi';

  constructor(private http: HttpClient) {}

  getKpis(): Observable<TechnicienKpiResponse> {
    return this.http.get<TechnicienKpiResponse>(this.apiUrl);
  }
}