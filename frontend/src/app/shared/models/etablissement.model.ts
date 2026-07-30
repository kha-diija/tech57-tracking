export interface ResponsableDto {
  idResponsable?: number | null;
  nom: string;
  prenom: string;
  fonction?: string;
  telephone?: string;
  email?: string;
}

export interface Etablissement {
  idEtablissement: number;
  reference: string;
  designation: string;
  type: string;
  localisationGps?: string; // format "lat,lng"
  nombreBeneficiaires?: number;
  telephoneContact?: string;
  emailContact?: string;

  idCommune: number;
  communeNom: string;
  idProvince: number;
  provinceNom: string;
  idRegion: number;
  regionNom: string;

  responsable?: ResponsableDto | null;
}

export interface EtablissementRequest {
  reference: string;
  designation: string;
  type: string;
  localisationGps?: string;
  nombreBeneficiaires?: number;
  telephoneContact?: string;
  emailContact?: string;
  idCommune: number;
  responsable?: ResponsableDto | null;
}

export interface EtablissementKpi {
  totalEtablissements: number;
  regionsCouvertes: number;
  totalBeneficiaires: number;
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
}