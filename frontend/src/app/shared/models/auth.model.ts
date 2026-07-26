export interface LoginRequest {
  email: string;
  motDePasse: string;
}

export type UserRole =
  | 'ADMINISTRATEUR'
  | 'TECHNICIEN'
  | 'OBSERVATEUR'
  | 'GESTIONNAIRE_STOCK';

export interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  id: number;
  nom: string;
  prenom: string;
  email: string;
  role: UserRole;
  redirectUrl: string;
}

export interface RefreshTokenRequest {
  refreshToken: string;
}

export interface CurrentUser {
  id: number;
  nom: string;
  prenom: string;
  email: string;
  role: UserRole;
}

export interface ApiErrorResponse {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
  fieldErrors?: Record<string, string>;
}
