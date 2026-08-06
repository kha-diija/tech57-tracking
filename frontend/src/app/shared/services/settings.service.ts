import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface ChangePasswordRequest {
  ancienMotDePasse: string;
  nouveauMotDePasse: string;
}

@Injectable({ providedIn: 'root' })
export class SettingsService {
  private apiUrl = '/api/user/settings';

  constructor(private http: HttpClient) {}

  changePassword(data: ChangePasswordRequest): Observable<{ message: string }> {
    return this.http.put<{ message: string }>(`${this.apiUrl}/change-password`, data);
  }
}