export interface Etablissement {
  id: number;
  reference: string;
  designation: string;
  type: 'École' | 'Collège' | 'Lycée' | 'Centre de formation';
  region: string;
  province: string;
  commune: string;
  beneficiaires: number;
  latitude: number;
  longitude: number;
  responsableNom: string;
  responsableContact: string;
  statutInstallation: 'Installé' | 'En cours' | 'Planifié' | 'Non démarré';
}