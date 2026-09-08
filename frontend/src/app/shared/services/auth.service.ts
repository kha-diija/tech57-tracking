import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  CurrentUser,
  LoginRequest,
  LoginResponse,
  MessageResponse,
  UserRole,
} from '../models/auth.model';

const ACCESS_TOKEN_KEY = 'tech57_access_token';
const REFRESH_TOKEN_KEY = 'tech57_refresh_token';
const CURRENT_USER_KEY = 'tech57_current_user';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly apiUrl = `${environment.apiUrl}/auth`;

  /** Signal réactif exposant l'utilisateur connecté (ou null). */
  readonly currentUser = signal<CurrentUser | null>(this.readStoredUser());

  constructor(private http: HttpClient, private router: Router) {}

  login(credentials: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.apiUrl}/login`, credentials).pipe(
      tap((response) => this.handleAuthSuccess(response))
    );
  }

  refreshToken(): Observable<LoginResponse> {
    const refreshToken = this.getRefreshToken();
    return this.http
      .post<LoginResponse>(`${this.apiUrl}/refresh`, { refreshToken })
      .pipe(tap((response) => this.handleAuthSuccess(response)));
  }

  // APRÈS — ajoute le paramètre reason et transmets-le
logout(reason?: string): void {
  const refreshToken = this.getRefreshToken();
  if (refreshToken) {
    this.http.post(`${this.apiUrl}/logout`, { refreshToken }).subscribe({
      complete: () => this.clearSessionAndRedirect(reason),
      error: () => this.clearSessionAndRedirect(reason),
    });
  } else {
    this.clearSessionAndRedirect(reason);
  }
}

  /**
   * Demande d'envoi d'un email de réinitialisation de mot de passe.
   * Le backend répond toujours 200 avec le même message, que l'email
   * existe ou non (pas d'énumération de comptes).
   */
  forgotPassword(email: string): Observable<MessageResponse> {
    return this.http.post<MessageResponse>(`${this.apiUrl}/forgot-password`, { email });
  }

  /**
   * Valide le token reçu par email et applique le nouveau mot de passe.
   */
  resetPassword(token: string, nouveauMotDePasse: string): Observable<MessageResponse> {
    return this.http.post<MessageResponse>(`${this.apiUrl}/reset-password`, {
      token,
      nouveauMotDePasse,
    });
  }

  isAuthenticated(): boolean {
    return !!this.getAccessToken();
  }

  hasRole(...roles: UserRole[]): boolean {
    const user = this.currentUser();
    return !!user && roles.includes(user.role);
  }

  getAccessToken(): string | null {
    return localStorage.getItem(ACCESS_TOKEN_KEY);
  }

  getRefreshToken(): string | null {
    return localStorage.getItem(REFRESH_TOKEN_KEY);
  }

  /** Route de redirection Angular renvoyée par le backend selon le rôle. */
  redirectAfterLogin(redirectUrl: string): void {
    this.router.navigateByUrl(redirectUrl);
  }

  private handleAuthSuccess(response: LoginResponse): void {
    localStorage.setItem(ACCESS_TOKEN_KEY, response.accessToken);
    localStorage.setItem(REFRESH_TOKEN_KEY, response.refreshToken);

    const user: CurrentUser = {
      id: response.id,
      nom: response.nom,
      prenom: response.prenom,
      email: response.email,
      role: response.role,
    };
    localStorage.setItem(CURRENT_USER_KEY, JSON.stringify(user));
    this.currentUser.set(user);
  }

  // APRÈS — accepte reason et le transmet via router state
private clearSessionAndRedirect(reason?: string): void {
  localStorage.removeItem(ACCESS_TOKEN_KEY);
  localStorage.removeItem(REFRESH_TOKEN_KEY);
  localStorage.removeItem(CURRENT_USER_KEY);
  this.currentUser.set(null);

  if (reason) {
    this.router.navigateByUrl('/login', { state: { sessionMessage: reason } });
  } else {
    this.router.navigateByUrl('/login');
  }
}

  private readStoredUser(): CurrentUser | null {
    const raw = localStorage.getItem(CURRENT_USER_KEY);
    return raw ? (JSON.parse(raw) as CurrentUser) : null;
  }
}