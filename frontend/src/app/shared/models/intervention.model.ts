export interface Photo {
  id: number;
  cheminFichier: string;
  typePhoto: string;
}

export interface Attestation {
  id: number;
  dateSignature: string;
  cheminFichier?: string;
  nomSignataire: string;
  valide: boolean;
  statut?: string;           // GENEREE, SIGNEE, VALIDEE
  dateGeneration?: string;
  cheminFichierSigne?: string;
  dateUploadSigne?: string;
}

// --- Aligné exactement sur CheckInOutDto.java ---
export interface CheckInOut {
  idCheckinout: number;
  numeroVisite: number;
  dateHeureCheckin: string | null;
  dateHeureCheckout: string | null;
  dureeMinutes: number | null;
  gpsCheckin: string | null;
  gpsCheckout: string | null;
}

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

// --- Aligné exactement sur InterventionResponse.java ---
export interface Intervention {
  id: number;
  datePrevue: string | null;   // ajouté : correspond à response.getDatePrevue()
  dateDebut: string | null;
  dateFin: string | null;
  tauxAvancement: number;
  numeroVisite: number;
  statut: string;              // 'Planifiée' | 'En cours' | 'En retard' | 'Exécutée' | 'Clôturée'
  localisationGps?: string;
  missionId: number;
  missionReference: string;
  etablissementDesignation?: string; // ajouté : présent dans InterventionResponse
  technicienId: number;
  technicienNom: string;

  photos?: Photo[];
  attestation?: Attestation;
  sortiesMateriel?: SortieMateriel[];
  retoursMateriel?: RetourMateriel[];
  checklistItems?: ChecklistItem[];

  // ajouté : correspond à response.getCheckInOuts()
  checkInOuts?: CheckInOut[];
}

// --- Aligné exactement sur CreateInterventionRequest.java / UpdateInterventionRequest.java ---
export interface InterventionRequest {
  datePrevue: string;    // obligatoire côté formulaire : c'est ce que l'admin fixe
  dateDebut?: string;    // optionnel : le backend l'accepte mais ne devrait plus être saisi à la main
  dateFin?: string;      // optionnel, idem
  tauxAvancement: number;
  numeroVisite: number;
  statut: string;
  localisationGps?: string;
  missionId: number;
  technicienId: number;
}
export interface TechnicienInterventionForm {
  id?: number;
  missionId: number | null;
  datePrevue: string;
  numeroVisite: number;
  tauxAvancement: number;
  statut: string;
  dateDebut?: string;
  dateFin?: string;
}