import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface StockKpi {
  id: string;
  label: string;
  value: number;
  suffix?: string;
  trend: number;
  trendUp: boolean;
  comparison: string;
}

export interface StockOutPoint {
  periode: string;
  quantite: number;
}

export interface StockDistributionItem {
  label: string;
  value: number;
  color: string;
}

export interface MaintenanceItem {
  idMateriel: number;
  reference: string;
  nom: string;
  etat: string;
  categorie: string;
  etablissement: string;
}

export interface LowStockItem {
  idMateriel: number;
  nom: string;
  reference: string;
  quantiteDisponible: number;
  seuilAlerte: number;
}

@Injectable({ providedIn: 'root' })
export class StockDashboardService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/gestionnaire-stock/dashboard';

  getKpis(): Observable<StockKpi[]> {
    return this.http.get<StockKpi[]>(`${this.baseUrl}/kpis`);
  }

  getWeeklyStockOut(): Observable<StockOutPoint[]> {
    return this.http.get<StockOutPoint[]>(`${this.baseUrl}/weekly-stock-out`);
  }

  getStockDistribution(): Observable<StockDistributionItem[]> {
    return this.http.get<StockDistributionItem[]>(`${this.baseUrl}/stock-distribution`);
  }

  getMaintenanceList(): Observable<MaintenanceItem[]> {
    return this.http.get<MaintenanceItem[]>(`${this.baseUrl}/maintenance`);
  }

  getLowStockAlerts(): Observable<LowStockItem[]> {
    return this.http.get<LowStockItem[]>(`${this.baseUrl}/low-stock`);
  }
}
