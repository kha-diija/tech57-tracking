export interface EtablissementAvancement {
  idEtablissement: number;
  designation: string;
  commune: string;
  nombreBeneficiaires: number;
  nombreMateriels: number;
  pourcentageAvancement: number;
}

export interface PartenaireDashboard {
  nomProvince: string;
  nombreEtablissements: number;
  totalBeneficiaires: number;
  avancementGlobal: number;
  etablissements: EtablissementAvancement[];
}