export interface LigneSortieDto {
  idMateriel: number;
  materielReference: string;
  materielNom: string;
  quantiteSortie: number;
}

export interface SortieARegulariserDto {
  idSortie: number;
  dateSortie: string;
  missionReference: string | null;
  technicienId: number | null;
  technicienNom: string | null;
  lignes: LigneSortieDto[];
}

export interface LigneRetourRequest {
  idMateriel: number;
  quantiteBonEtat: number;
  quantiteEnPanne: number;
}

export interface ValiderRetourRequest {
  lignes: LigneRetourRequest[];
}