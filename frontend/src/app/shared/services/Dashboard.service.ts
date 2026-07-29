import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface KpiResponse {
  id: string;
  label: string;
  value: number;
  suffix?: string;
  trend: number;
  trendUp: boolean;
  comparison: string;
}

export interface WeeklyMissionPoint {
  day: string;
  planned: number;
  completed: number;
}

export interface InstallationPoint {
  week: string;
  value: number;
}

export interface MaterialDistributionItem {
  label: string;
  value: number;
  color: string;
}

export interface ActivityItem {
  id: string;
  author: string;
  action: string;
  target: string;
  time: string;
  status: string;
}

export interface UpcomingMission {
  code: string;
  title: string;
  subtitle: string;
  time: string;
  technicien: string;
}

@Injectable({
  providedIn: 'root'
})
export class DashboardService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/admin/dashboard';

  getKpis(period = '7d'): Observable<KpiResponse[]> {
    const params = new HttpParams().set('period', period);
    return this.http.get<KpiResponse[]>(`${this.baseUrl}/kpis`, { params });
  }

  getWeeklyMissions(): Observable<WeeklyMissionPoint[]> {
    return this.http.get<WeeklyMissionPoint[]>(`${this.baseUrl}/weekly-missions`);
  }

  getInstallationProgress(): Observable<InstallationPoint[]> {
    return this.http.get<InstallationPoint[]>(`${this.baseUrl}/installation-progress`);
  }

  getMaterialDistribution(): Observable<MaterialDistributionItem[]> {
    return this.http.get<MaterialDistributionItem[]>(`${this.baseUrl}/material-distribution`);
  }

  getRecentActivity(): Observable<ActivityItem[]> {
    return this.http.get<ActivityItem[]>(`${this.baseUrl}/recent-activity`);
  }

  getUpcomingMissions(): Observable<UpcomingMission[]> {
    return this.http.get<UpcomingMission[]>(`${this.baseUrl}/upcoming-missions`);
  }

  // --- Nouvelles actions ---
  exportDashboardData(): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/export`, { responseType: 'blob' });
  }

  createMission(missionData: any): Observable<any> {
    return this.http.post(`${this.baseUrl}/missions`, missionData);
  }
}