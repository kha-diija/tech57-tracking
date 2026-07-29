export type EtatMateriel = 'Neuf' | 'En service' | 'En panne' | 'Retiré';

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number; // page courante (0-based)
  size: number;
}

export interface CategorieMateriel {
  idCategorie: number;
  nom: string;
  estKit: boolean;
}

export interface MaterielDTO {
  idMateriel: number;
  reference: string;
  nom: string;
  numeroSerie: string | null;
  codeQr: string | null;
  etat: EtatMateriel;

  idCategorie: number | null;
  nomCategorie: string | null;
  estKit: boolean;

  idEtablissement: number | null;
  designationEtablissement: string | null;

  idMaterielParent: number | null;
  quantiteComposant: number | null;

  composants: MaterielDTO[] | null;
  enMaintenance: boolean;
}

export interface MaterielRequest {
  reference: string;
  nom: string;
  numeroSerie?: string | null;
  codeQr?: string | null;
  etat?: EtatMateriel | null;
  idCategorie: number;
  idEtablissement?: number | null;
}

export interface KitRequest {
  reference: string;
  nom: string;
  numeroSerie?: string | null;
  codeQr?: string | null;
  idCategorie: number;
  idEtablissement?: number | null;
  composantsHeritentQr?: boolean;
  composants?: string[] | null;
}

export interface ComposantRequest {
  nom: string;
  reference?: string | null;
  numeroSerie?: string | null;
  codeQr?: string | null;
  quantiteComposant?: number;
}

export interface MaintenanceDTO {
  idMaintenance: number;
  dateMaintenance: string; // ISO yyyy-MM-dd
  description: string | null;
  cout: number | null;
  disponible: boolean;
  idMateriel: number;
  referenceMateriel: string;
}

export interface MaintenanceRequest {
  dateMaintenance: string;
  description?: string | null;
  cout?: number | null;
  disponible?: boolean;
  idMateriel: number;
}

export interface MouvementMateriel {
  idMouvement: number;
  type: string;
  dateMouvement: string;
  origine: string | null;
  destination: string | null;
}

export const ETATS: EtatMateriel[] = ['Neuf', 'En service', 'En panne', 'Retiré'];

export const KIT_COMPOSANTS_DEFAUT = [
  "Guide d'utilisation",
  'Sachet des matériaux',
  'Câbles USB',
  'Chargeur',
  'Batterie VEX GO',
];