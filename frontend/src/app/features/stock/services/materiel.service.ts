import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import {
  CategorieMateriel,
  ComposantRequest,
  KitRequest,
  MaintenanceDTO,
  MaintenanceRequest,
  MaterielDTO,
  MaterielRequest,
  MouvementMateriel,
  Page,
} from '../models/materiel.model';

export interface RechercheMaterielParams {
  search?: string;
  etat?: string;
  idCategorie?: number;
  idEtablissement?: number;
  topLevelOnly?: boolean;
  page?: number;
  size?: number;
  sort?: string;
}

@Injectable({ providedIn: 'root' })
export class MaterielService {
  private http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/materiels`;
  private readonly maintenanceBase = `${environment.apiUrl}/maintenances`;
  private readonly categorieBase = `${environment.apiUrl}/categories-materiel`;

  rechercher(params: RechercheMaterielParams): Observable<Page<MaterielDTO>> {
    let httpParams = new HttpParams();
    if (params.search) httpParams = httpParams.set('search', params.search);
    if (params.etat) httpParams = httpParams.set('etat', params.etat);
    if (params.idCategorie) httpParams = httpParams.set('idCategorie', params.idCategorie);
    if (params.idEtablissement) httpParams = httpParams.set('idEtablissement', params.idEtablissement);
    httpParams = httpParams.set('topLevelOnly', params.topLevelOnly ?? true);
    httpParams = httpParams.set('page', params.page ?? 0);
    httpParams = httpParams.set('size', params.size ?? 24);
    httpParams = httpParams.set('sort', params.sort ?? 'nom,asc');
    return this.http.get<Page<MaterielDTO>>(this.base, { params: httpParams });
  }

  getById(id: number): Observable<MaterielDTO> {
    return this.http.get<MaterielDTO>(`${this.base}/${id}`);
  }

  creerSimple(payload: MaterielRequest): Observable<MaterielDTO> {
    return this.http.post<MaterielDTO>(this.base, payload);
  }

  creerKit(payload: KitRequest): Observable<MaterielDTO> {
    return this.http.post<MaterielDTO>(`${this.base}/kits`, payload);
  }

  modifier(id: number, payload: MaterielRequest): Observable<MaterielDTO> {
    return this.http.put<MaterielDTO>(`${this.base}/${id}`, payload);
  }

  supprimer(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }

  changerEtat(id: number, etat: string): Observable<MaterielDTO> {
    return this.http.patch<MaterielDTO>(`${this.base}/${id}/etat`, { etat });
  }

  marquerEnMaintenance(id: number): Observable<MaterielDTO> {
    return this.http.patch<MaterielDTO>(`${this.base}/${id}/marquer-maintenance`, {});
  }

  ajouterComposant(idKit: number, payload: ComposantRequest): Observable<MaterielDTO> {
    return this.http.post<MaterielDTO>(`${this.base}/${idKit}/composants`, payload);
  }

  retirerComposant(idComposant: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/composants/${idComposant}`);
  }

  regenererCodeQr(id: number): Observable<{ codeQr: string }> {
    return this.http.post<{ codeQr: string }>(`${this.base}/${id}/code-qr`, {});
  }

  // --- Catégories ---
  getCategories(): Observable<CategorieMateriel[]> {
    return this.http.get<CategorieMateriel[]>(this.categorieBase);
  }

  // --- Maintenance ---
  getHistoriqueMaintenance(idMateriel: number): Observable<MaintenanceDTO[]> {
    return this.http.get<MaintenanceDTO[]>(`${this.maintenanceBase}/materiel/${idMateriel}`);
  }

  creerMaintenance(payload: MaintenanceRequest): Observable<MaintenanceDTO> {
    return this.http.post<MaintenanceDTO>(this.maintenanceBase, payload);
  }

  cloturerMaintenance(id: number): Observable<MaintenanceDTO> {
    return this.http.patch<MaintenanceDTO>(`${this.maintenanceBase}/${id}/cloturer`, {});
  }

  supprimerMaintenance(id: number): Observable<void> {
    return this.http.delete<void>(`${this.maintenanceBase}/${id}`);
  }

  // --- Mouvements ---
  getHistoriqueMouvements(idMateriel: number): Observable<MouvementMateriel[]> {
    return this.http.get<MouvementMateriel[]>(`${this.base}/${idMateriel}/mouvements`);
  }
}