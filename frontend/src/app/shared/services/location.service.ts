import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Region, Province, Commune } from '../models/etablissement.model';

@Injectable({ providedIn: 'root' })
export class LocationService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/admin/locations`;

  getRegions(): Observable<Region[]> {
    return this.http.get<Region[]>(`${this.apiUrl}/regions`);
  }

  getProvinces(regionId?: number): Observable<Province[]> {
    let params = new HttpParams();
    if (regionId) params = params.set('regionId', regionId);
    return this.http.get<Province[]>(`${this.apiUrl}/provinces`, { params });
  }

  getCommunes(provinceId?: number): Observable<Commune[]> {
    let params = new HttpParams();
    if (provinceId) params = params.set('provinceId', provinceId);
    return this.http.get<Commune[]>(`${this.apiUrl}/communes`, { params });
  }
}