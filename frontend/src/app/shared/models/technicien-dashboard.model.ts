export interface MissionActuelle {
  id: number;
  titre: string;
  etablissement: string;
  horaire: string | null;
  urgence: string;
  statut: string;
}

export interface EtablissementAssigne {
  id: number;
  nom: string;
  ville: string;
  interventions: number;
  etat: string | null;
}

export interface TechnicienKpiResponse {
  missionsJour: number;
  etablissementsCount: number;
  enAttente: number;
  missionsActuelles: MissionActuelle[];
  etablissementsAssignes: EtablissementAssigne[];

  interventionsRealisees: number;
  interventionsEnCours: number;
  interventionsEnRetard: number;
  tauxAvancementMoyen: number;
  tauxConformite: number;
  tempsMoyenInterventionMinutes: number;
  anomaliesDetectees: number;

  quantiteMaterielSortie: number;
  quantiteMaterielRendue: number;
}