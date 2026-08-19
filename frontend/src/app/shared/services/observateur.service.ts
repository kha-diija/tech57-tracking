import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { VideoAssignment, ResourceAssignment, DocumentAssignment } from '../models/permission.model';

export interface ObservateurDashboardSummary {
  totalVideos: number;
  totalRessources: number;
  totalDocuments: number;
  totalElements: number;
  dernierAssignation: string | null;
}

export interface AssignmentDistributionItem {
  label: string;
  value: number;
  color: string;
}

export interface AssignmentTimelinePoint {
  periode: string;
  total: number;
}

@Injectable({ providedIn: 'root' })
export class ObservateurService {
  private http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/observateur`;

  getMesVideos(): Observable<VideoAssignment[]> {
    return this.http.get<VideoAssignment[]>(`${this.baseUrl}/videos`);
  }

  getMesRessources(): Observable<ResourceAssignment[]> {
    return this.http.get<ResourceAssignment[]>(`${this.baseUrl}/resources`);
  }

  getMesDocuments(): Observable<DocumentAssignment[]> {
    return this.http.get<DocumentAssignment[]>(`${this.baseUrl}/documents`);
  }

  getDashboardSummary(): Observable<ObservateurDashboardSummary> {
    return this.http.get<ObservateurDashboardSummary>(`${this.baseUrl}/dashboard/summary`);
  }

  getDistribution(): Observable<AssignmentDistributionItem[]> {
    return this.http.get<AssignmentDistributionItem[]>(`${this.baseUrl}/dashboard/distribution`);
  }

  getTimeline(): Observable<AssignmentTimelinePoint[]> {
    return this.http.get<AssignmentTimelinePoint[]>(`${this.baseUrl}/dashboard/timeline`);
  }
  
  viewDocument(idDocument: number) {
  return this.http.get(`${this.baseUrl}/documents/${idDocument}/view`, {
    responseType: 'blob'
  });
}
}