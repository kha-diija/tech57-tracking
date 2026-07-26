import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { delay } from 'rxjs/operators';

export interface KpiCard {
  id: string;
  label: string;
  value: number;
  suffix?: string;
  trend: number;
  trendUp: boolean;
  comparison: string;
}

export interface WeeklyMissionPoint {
  day: string;
  planned: number;
  completed: number;
}

export interface InstallationPoint {
  week: string;
  value: number;
}

export interface MaterialDistributionItem {
  label: string;
  value: number;
  color: string; // couleur hex directe (utilisée dans le conic-gradient du donut)
}

export interface ActivityItem {
  id: string;
  author: string;
  action: string;
  target: string;
  time: string;
  status: 'success' | 'warning' | 'info';
}

export interface UpcomingMission {
  code: string;
  title: string;
  subtitle: string;
  time: string;
  technicien: string;
}

export interface AnomalyItem {
  id: string;
  label: string;
  etablissement: string;
  severity: 'critique' | 'moyenne' | 'faible';
}

/**
 * TODO(backend): remplacer les `of(...)` par de vrais appels HttpClient
 * une fois les endpoints Spring Boot dispo, ex:
 *   getKpis(): Observable<KpiCard[]> {
 *     return this.http.get<KpiCard[]>(`${environment.apiUrl}/admin/dashboard/kpis`);
 *   }
 * Les interfaces ci-dessus sont déjà calées sur ce que l'API devra renvoyer,
 * donc aucun composant n'aura à changer, seule cette classe bouge.
 */
@Injectable({ providedIn: 'root' })
export class DashboardService {
  private readonly fakeLatency = 300;

  getKpis(): Observable<KpiCard[]> {
    return of([
      {
        id: 'missions',
        label: 'Missions actives',
        value: 128,
        trend: 12,
        trendUp: true,
        comparison: 'vs 7 derniers jours'
      },
      {
        id: 'etablissements',
        label: 'Établissements suivis',
        value: 342,
        trend: 4.2,
        trendUp: true,
        comparison: 'sur le territoire'
      },
      {
        id: 'techniciens',
        label: 'Techniciens sur le terrain',
        value: 47,
        suffix: '',
        trend: 98,
        trendUp: true,
        comparison: "taux de disponibilité"
      },
      {
        id: 'conformite',
        label: 'Taux de conformité',
        value: 94.6,
        suffix: '%',
        trend: 2.6,
        trendUp: true,
        comparison: 'objectif 92%'
      }
    ]).pipe(delay(this.fakeLatency));
  }

  getWeeklyMissions(): Observable<WeeklyMissionPoint[]> {
    return of([
      { day: 'Lun', planned: 42, completed: 34 },
      { day: 'Mar', planned: 51, completed: 40 },
      { day: 'Mer', planned: 60, completed: 52 },
      { day: 'Jeu', planned: 38, completed: 30 },
      { day: 'Ven', planned: 58, completed: 55 },
      { day: 'Sam', planned: 25, completed: 22 },
      { day: 'Dim', planned: 14, completed: 14 }
    ]).pipe(delay(this.fakeLatency));
  }

  getInstallationProgress(): Observable<InstallationPoint[]> {
    return of([
      { week: 'S1', value: 120 },
      { week: 'S2', value: 260 },
      { week: 'S3', value: 410 },
      { week: 'S4', value: 590 },
      { week: 'S5', value: 780 },
      { week: 'S6', value: 980 },
      { week: 'S7', value: 1180 },
      { week: 'S8', value: 1400 }
    ]).pipe(delay(this.fakeLatency));
  }

  getMaterialDistribution(): Observable<MaterialDistributionItem[]> {
    return of([
      { label: 'Installé', value: 3412, color: '#e85002' },
      { label: 'En transit', value: 412, color: '#ff9d76' },
      { label: 'En stock', value: 890, color: '#2a2f3d' }
    ]).pipe(delay(this.fakeLatency));
  }

  getRecentActivity(): Observable<ActivityItem[]> {
    return of<ActivityItem[]>([
      {
        id: 'a1',
        author: 'K. El Amrani',
        action: 'a terminé l\u2019installation à',
        target: 'Lycée Ibn Sina',
        time: 'Il y a 2 min',
        status: 'success'
      },
      {
        id: 'a2',
        author: 'S. Bennani',
        action: 's\u2019est enregistré(e) à',
        target: 'École Al Farabi',
        time: 'Il y a 9 min',
        status: 'info'
      },
      {
        id: 'a3',
        author: 'M. Zaki',
        action: 'a signalé un routeur manquant à',
        target: 'Collège Anfa',
        time: 'Il y a 22 min',
        status: 'warning'
      },
      {
        id: 'a4',
        author: 'L. Tazi',
        action: 'a téléversé 12 photos après-installation \u2014 mission',
        target: '#M-8842',
        time: 'Il y a 1h',
        status: 'success'
      }
    ]).pipe(delay(this.fakeLatency));
  }

  getUpcomingMissions(): Observable<UpcomingMission[]> {
    return of([
      {
        code: 'M-8901',
        title: 'Université Hassan II \u2014 Bloc C',
        subtitle: 'K. El Amrani',
        time: '10:30',
        technicien: 'K. El Amrani'
      },
      {
        code: 'M-8902',
        title: 'École Al Wahda',
        subtitle: 'S. Bennani',
        time: '11:15',
        technicien: 'S. Bennani'
      },
      {
        code: 'M-8903',
        title: 'Collège Ibn Rochd',
        subtitle: 'M. Zaki',
        time: '13:00',
        technicien: 'M. Zaki'
      }
    ]).pipe(delay(this.fakeLatency));
  }

  getAnomalies(): Observable<AnomalyItem[]> {
    return of<AnomalyItem[]>([
      { id: 'an1', label: 'Routeur manquant', etablissement: 'Collège Anfa', severity: 'critique' },
      { id: 'an2', label: 'Câble endommagé', etablissement: 'École Al Farabi', severity: 'critique' },
      { id: 'an3', label: 'Datashow non configuré', etablissement: 'Lycée Ibn Sina', severity: 'moyenne' }
    ]).pipe(delay(this.fakeLatency));
  }
}