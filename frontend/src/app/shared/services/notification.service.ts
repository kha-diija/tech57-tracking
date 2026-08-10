import { Injectable, inject, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Client, IMessage } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { environment } from '../../../environments/environment';
import { AuthService } from './auth.service';
import { NotificationDto } from '../models/notification.model';

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private http = inject(HttpClient);
  private authService = inject(AuthService);
  private readonly base = `${environment.apiUrl}/notifications`;

  private stompClient: Client | null = null;

  readonly notifications = signal<NotificationDto[]>([]);
  readonly nonLuesCount = computed(() =>
    this.notifications().filter((n) => !n.lu).length
  );

  /** À appeler une fois après le login (ou au démarrage si déjà connecté). */
  initialiser(): void {
    this.chargerHistorique();
    this.connecterWebSocket();
  }

  chargerHistorique(): void {
    this.http.get<NotificationDto[]>(this.base).subscribe((res) => {
      this.notifications.set(res);
    });
  }

  marquerCommeLue(id: number): void {
    this.http.patch(`${this.base}/${id}/lu`, {}).subscribe(() => {
      this.notifications.update((list) =>
        list.map((n) => (n.idNotification === id ? { ...n, lu: true } : n))
      );
    });
  }

  marquerToutesCommeLues(): void {
    this.http.patch(`${this.base}/lu-toutes`, {}).subscribe(() => {
      this.notifications.update((list) => list.map((n) => ({ ...n, lu: true })));
    });
  }

  private connecterWebSocket(): void {
    const token = this.authService.getAccessToken();
    if (!token) return;

    // environment.apiUrl est du type http://localhost:8080/api → on retire /api pour le endpoint /ws
    const wsBaseUrl = environment.apiUrl.replace(/\/api\/?$/, '');

    this.stompClient = new Client({
      webSocketFactory: () => new SockJS(`${wsBaseUrl}/ws`) as any,
      connectHeaders: { Authorization: `Bearer ${token}` },
      reconnectDelay: 5000,
      onConnect: () => {
        this.stompClient?.subscribe('/user/queue/notifications', (message: IMessage) => {
          const notif: NotificationDto = JSON.parse(message.body);
          this.notifications.update((list) => [notif, ...list]);
        });
      },
    });

    this.stompClient.activate();
  }

  deconnecter(): void {
    this.stompClient?.deactivate();
    this.stompClient = null;
    this.notifications.set([]);
  }
}