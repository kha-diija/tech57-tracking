export interface Photo {
  id: number;
  cheminFichier: string;
  typePhoto: string;
}

export interface Attestation {
  id: number;
  dateSignature: string;
  signatureNumerique: string;
  nomSignataire: string;
  valide: boolean;
}

// --- NOUVEAU : Interfaces pour le Stock ---
export interface SortieMateriel {
  idSortie: number;
  materielReference: string;
  quantite: number;
  dateSortie: string;
}

export interface RetourMateriel {
  idRetour: number;
  materielReference: string;
  quantite: number;
  etatMateriel: string;
  dateRetour: string;
}

export interface ChecklistItem {
  idItem: number;
  materielReference: string;
  quantite: number;
  etatConstate: string;
  conforme: boolean;
}

export interface Intervention {
  id: number;
  dateDebut: string;
  dateFin?: string;
  tauxAvancement: number;
  numeroVisite: number;
  statut: string;
  localisationGps?: string;
  missionId: number;
  missionReference: string;
  technicienId: number;
  technicienNom: string;

  // --- CHAMPS EXISTANTS ---
  photos?: Photo[];
  attestation?: Attestation;

  // --- NOUVEAUX CHAMPS STOCK ---
  sortiesMateriel?: SortieMateriel[];
  retoursMateriel?: RetourMateriel[];
  checklistItems?: ChecklistItem[];
}

export interface InterventionRequest {
  dateDebut: string;
  dateFin?: string;
  tauxAvancement: number;
  numeroVisite: number;
  statut: string;
  localisationGps?: string;
  missionId: number;
  technicienId: number;
}