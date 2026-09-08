import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { BackendUserDto, CreateGsUserDto, GsUser, UpdateGsUserDto } from '../models/gsuser.model';

@Injectable({
  providedIn: 'root'
})
export class GsUserService {
  private http = inject(HttpClient);
  private apiUrl = '/api/users';

  getUsers(): Observable<GsUser[]> {
    return this.http
      .get<BackendUserDto[]>(this.apiUrl)
      .pipe(map((list) => list.map(this.fromBackend)));
  }

  createGsUser(userData: CreateGsUserDto): Observable<GsUser> {
    const payload = {
      prenom: userData.firstname,
      nom: userData.lastname,
      email: userData.email,
      telephone: userData.telephone,
      motDePasse: userData.password,
      typeUtilisateur: userData.role
    };
    return this.http
      .post<BackendUserDto>(this.apiUrl, payload)
      .pipe(map(this.fromBackend));
  }

  /**
   * Met à jour les infos d'un utilisateur (nom, prénom, email, téléphone)
   * NE met pas à jour le mot de passe ni le rôle
   */
  updateGsUser(id: number, userData: UpdateGsUserDto): Observable<GsUser> {
    const payload = {
      prenom: userData.firstname,
      nom: userData.lastname,
      email: userData.email,
      telephone: userData.telephone
    };
    return this.http
      .put<BackendUserDto>(`${this.apiUrl}/${id}`, payload)
      .pipe(map(this.fromBackend));
  }

  /**
   * Modifie l'état d'activation (Actif <-> Bloqué)
   */
  toggleStatus(id: number): Observable<GsUser> {
    return this.http
      .patch<BackendUserDto>(`${this.apiUrl}/${id}/toggle-status`, {})
      .pipe(map(this.fromBackend));
  }

  deleteGsUser(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  private fromBackend(dto: BackendUserDto): GsUser {
    return {
      id: dto.id,
      firstname: dto.prenom,
      lastname: dto.nom,
      email: dto.email,
      telephone: dto.telephone,
      role: dto.typeUtilisateur,
      isActive: dto.compteActif,
      dateCreation: dto.dateCreation
    };
  }
}
