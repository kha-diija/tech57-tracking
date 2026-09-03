export interface NotificationDto {
  idNotification: number;
  message: string;
  dateEnvoi: string;
  lu: boolean;
  type: string;
  expediteurNom: string | null;
}