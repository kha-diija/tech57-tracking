export interface Materiel {
  idMateriel: number;
  reference: string;
  nom: string;
  numeroSerie?: string;
  codeQr?: string;
  etat: string;
  idCategorie?: number;
  nomCategorie?: string;
  quantiteComposant?: number;
  estKit?: boolean;
}