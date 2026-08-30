export interface ResponsableDto {
  idResponsable?: number | null;
  nom: string;
  prenom: string;
  fonction?: string;
  telephone?: string;
}

export interface Etablissement {
  idEtablissement: number;
  reference: string;
  designation: string;
  type: string;
  localisationGps?: string;
  nombreBeneficiaires?: number;
  nombreBeneficiairesReel?: number | null; // ✅ AJOUT : aligné sur EtablissementResponse.java
  telephoneContact?: string;

  idCommune: number;
  communeNom: string;
  idProvince: number;
  provinceNom: string;
  idRegion: number;
  regionNom: string;

  responsable?: ResponsableDto | null;
  nbFormateurs?: number;
}

export interface EtablissementRequest {
  reference: string;
  designation: string;
  type: string;
  localisationGps?: string;
  nombreBeneficiaires?: number;
  telephoneContact?: string;
  idCommune: number;
  responsable?: ResponsableDto | null;
}

// APRÈS — ajoute cette ligne
export interface EtablissementKpi {
  totalEtablissements: number;
  regionsCouvertes: number;
  totalBeneficiaires: number;
  totalBeneficiairesReel: number;
  sansResponsable: number;
}

export interface Region {
  idRegion: number;
  nom: string;
  code: string;
}

export interface Province {
  idProvince: number;
  nom: string;
  code: string;
  idRegion: number;
}

export interface Commune {
  idCommune: number;
  nom: string;
  code: string;
  idProvince: number;
  provinceNom?: string;  // ✅ AJOUT : correspond au DTO
}


export interface ImportResult {
  totalLignes: number;
  crees: number;
  misAJour: number;
  ignores: number;
  erreurs: string[];
}
export interface Formateur {
  idFormateur: number;
  nom: string;
  prenom: string;
  telephone?: string;
  adresse?: string;
  email?: string;
}

export interface FormateurRequest {
  nom: string;
  prenom: string;
  telephone?: string;
  adresse?: string;
  email?: string;
}