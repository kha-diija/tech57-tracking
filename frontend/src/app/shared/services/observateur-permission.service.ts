import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  ObservateurSummary,
  VideoAssignment,
  ResourceAssignment,
  DocumentAssignment,
  VideoCatalogItem,
  RessourceCatalogItem,
  DocumentCatalogItem,
  CreateVideoAssignmentRequest,
  CreateResourceAssignmentRequest,
  CreateDocumentAssignmentRequest,
} from '../models/permission.model';

@Injectable({ providedIn: 'root' })
export class ObservateurPermissionService {
  private http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/admin/permissions`;

  getObservateurs(): Observable<ObservateurSummary[]> {
    return this.http.get<ObservateurSummary[]>(`${this.baseUrl}/observateurs`);
  }

  // ---- Assignations existantes ----
  getVideosAssignedTo(idObservateur: number): Observable<VideoAssignment[]> {
    return this.http.get<VideoAssignment[]>(`${this.baseUrl}/observateurs/${idObservateur}/videos`);
  }

  getResourcesAssignedTo(idObservateur: number): Observable<ResourceAssignment[]> {
    return this.http.get<ResourceAssignment[]>(`${this.baseUrl}/observateurs/${idObservateur}/resources`);
  }

  getDocumentsAssignedTo(idObservateur: number): Observable<DocumentAssignment[]> {
    return this.http.get<DocumentAssignment[]>(`${this.baseUrl}/observateurs/${idObservateur}/documents`);
  }

  // ---- Catalogues ----
  getVideoCatalog(): Observable<VideoCatalogItem[]> {
    return this.http.get<VideoCatalogItem[]>(`${this.baseUrl}/catalog/videos`);
  }

  getResourceCatalog(): Observable<RessourceCatalogItem[]> {
    return this.http.get<RessourceCatalogItem[]>(`${this.baseUrl}/catalog/resources`);
  }

  getDocumentCatalog(): Observable<DocumentCatalogItem[]> {
    return this.http.get<DocumentCatalogItem[]>(`${this.baseUrl}/catalog/documents`);
  }

  // ---- Assigner ----
  assignVideo(request: CreateVideoAssignmentRequest): Observable<VideoAssignment> {
    return this.http.post<VideoAssignment>(`${this.baseUrl}/videos`, request);
  }

  assignResource(request: CreateResourceAssignmentRequest): Observable<ResourceAssignment> {
    return this.http.post<ResourceAssignment>(`${this.baseUrl}/resources`, request);
  }

  assignDocument(request: CreateDocumentAssignmentRequest): Observable<DocumentAssignment> {
    return this.http.post<DocumentAssignment>(`${this.baseUrl}/documents`, request);
  }

  // ---- Révoquer ----
  revokeVideo(idObservateur: number, idVideo: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/videos/${idObservateur}/${idVideo}`);
  }

  revokeResource(idObservateur: number, idRessource: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/resources/${idObservateur}/${idRessource}`);
  }

  revokeDocument(idObservateur: number, idDocument: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/documents/${idObservateur}/${idDocument}`);
  }
}