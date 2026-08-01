import { Etablissement } from './etablissement.model'; // Adapte le chemin selon ton projet

export interface MissionInstallation {
  idMission: number;
  reference: string;
  titre: string;
  statut: string;
  dateCreation: string;
  budgetPropose?: number;
  
  // Correspond aux champs aplatis du DTO de réponse back-end
  idEtablissement: number;
  etablissementDesignation: string;
  etablissementReference: string;

  idAdministrateur: number;
  adminNomComplet: string;

  idEquipe?: number;
  equipeNom?: string;
}

export interface MissionRequestDTO {
  reference: string;
  titre: string;
  statut: string;
  budgetPropose?: number | null;
  idEtablissement: number;
  idAdministrateur: number;
  idEquipe?: number | null;
}