export interface SortieMaterielDetailDto {
  idDetail: number;
  idMateriel: number;
  materielReference: string;
  materielNom: string;
  quantiteDemandee: number;
  stockDisponible: number | null;
}

export interface SortieMaterielDto {
  idSortie: number;
  dateSortie: string;
  lieuIntervention: string | null;
  statut: string; // 'En attente' | 'Validée' | 'Rejetée'
  motifRejet: string | null;
  technicienId: number | null;
  technicienNom: string | null;
  interventionId: number | null;
  missionReference: string | null;
  details: SortieMaterielDetailDto[];
}

export interface RejeterSortieRequest {
  motifRejet: string;
}