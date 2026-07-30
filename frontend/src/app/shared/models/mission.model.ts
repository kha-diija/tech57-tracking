export interface Mission {
  idMission: number;
  reference: string;
  titre: string;
  etablissementNom: string;
  equipeAffectee: string;
  datePrevue: string;
  priorite: 'BASSE' | 'NORMALE' | 'HAUTE' | 'URGENTE';
  statut: 'BROUILLON' | 'VALIDEE' | 'EN_COURS' | 'TERMINEE';
  budgetEstime: number;
}

export interface MissionKpi {
  totalMissions: number;
  validees: number;
  enCours: number;
  terminees: number;
}

export type MissionPayload = Omit<Mission, 'idMission'>;