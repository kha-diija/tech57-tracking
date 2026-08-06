export interface AchatMaterielDto {
  idAchat: number;
  numeroFacture: string | null;
  fournisseur: string | null;
  quantite: number;
  prixUnitaireHt: number | null;
  dateAchat: string;

  idMateriel: number;
  materielReference: string;
  materielNom: string;

  acheteurNom: string | null;
}

export interface CreerAchatRequest {
  idMateriel: number;
  quantite: number;
  fournisseur?: string | null;
  numeroFacture?: string | null;
  prixUnitaireHt?: number | null;
}