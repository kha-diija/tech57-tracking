export type GsUserRole = 'ADMINISTRATEUR' | 'TECHNICIEN' | 'OBSERVATEUR' | 'GESTIONNAIRE_STOCK';

export interface GsUser {
  id: number;
  firstname: string;
  lastname: string;
  email: string;
  telephone?: string;
  role: GsUserRole;
  isActive: boolean;
  dateCreation?: string;
}

export interface CreateGsUserDto {
  firstname: string;
  lastname: string;
  email: string;
  telephone?: string;
  role: GsUserRole;
  password: string;
}

/**
 * Forme exacte renvoyée par le backend (UserResponseDto).
 * Sert uniquement à la conversion dans GsUserService — ne pas utiliser
 * directement dans les composants, utilise `GsUser`.
 */
export interface BackendUserDto {
  id: number;
  nom: string;
  prenom: string;
  email: string;
  telephone?: string;
  typeUtilisateur: GsUserRole;
  compteActif: boolean;
  dateCreation?: string;
}
