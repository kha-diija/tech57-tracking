import { UserRole } from './auth.model';

export interface ObservateurSummary {
  id: number;
  nom: string;
  prenom: string;
  email: string;
  typeClient: string;
}

export interface VideoAssignment {
  id: number;
  observateur: ObservateurSummary;
  idVideo: number;
  titreVideo: string;
  dateAssignation: string;
  actif: boolean;
  assigneParAdminNom: string;
}

export interface ResourceAssignment {
  id: number;
  observateur: ObservateurSummary;
  idRessource: number;
  titreRessource: string;
  typeRessource: string;
  dateAssignation: string;
  actif: boolean;
  assigneParAdminNom: string;
}

export interface DocumentAssignment {
  id: number;
  observateur: ObservateurSummary;
  idDocument: number;
  nomFichier: string;
  typeDocument: string;
  dateAssignation: string;
  actif: boolean;
  assigneParAdminNom: string;
}

export interface VideoCatalogItem {
  idVideo: number;
  titre: string;
  fournisseur: string;
  dureeSecondes?: number;
}

export interface RessourceCatalogItem {
  idRessource: number;
  titre: string;
  type: string;
}

export interface DocumentCatalogItem {
  idSource: number;
  nomFichier: string;
  typeSource: string;
}

export interface CreateVideoAssignmentRequest {
  idObservateur: number;
  idVideo: number;
}

export interface CreateResourceAssignmentRequest {
  idObservateur: number;
  idRessource: number;
}

export interface CreateDocumentAssignmentRequest {
  idObservateur: number;
  idDocument: number;
}