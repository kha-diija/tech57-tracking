import { Component, computed, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { toSignal } from '@angular/core/rxjs-interop';
import {
  LucideAngularModule,
  Briefcase,
  Building2,
  Users,
  ShieldCheck,
  ArrowUpRight,
  ArrowDownRight,
  Filter,
  Download,
  Plus,
  CheckCircle2,
  MapPin,
  AlertTriangle,
  Clock
} from 'lucide-angular';
import { AuthService } from '../../shared/services/auth.service';
import { DashboardService } from '../../shared/services/Dashboard.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, LucideAngularModule],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.scss'
})
export class Dashboard {
  private readonly authService = inject(AuthService);
  private readonly dashboardService = inject(DashboardService);

  readonly icons = {
    Briefcase,
    Building2,
    Users,
    ShieldCheck,
    ArrowUpRight,
    ArrowDownRight,
    Filter,
    Download,
    Plus,
    CheckCircle2,
    MapPin,
    AlertTriangle,
    Clock
  };

  private readonly kpiIconMap: Record<string, typeof Briefcase> = {
    missions: Briefcase,
    etablissements: Building2,
    techniciens: Users,
    conformite: ShieldCheck
  };

  readonly prenomAdmin = computed(() => {
    const user = this.authService.currentUser();
    return user?.prenom || user?.nom || 'Administrateur';
  });

  // --- Données (signals dérivés des observables du service) ---
  readonly kpis = toSignal(this.dashboardService.getKpis(), { initialValue: [] });
  readonly weeklyMissions = toSignal(this.dashboardService.getWeeklyMissions(), { initialValue: [] });
  readonly installationProgress = toSignal(this.dashboardService.getInstallationProgress(), { initialValue: [] });
  readonly materialDistribution = toSignal(this.dashboardService.getMaterialDistribution(), { initialValue: [] });
  readonly recentActivity = toSignal(this.dashboardService.getRecentActivity(), { initialValue: [] });
  readonly upcomingMissions = toSignal(this.dashboardService.getUpcomingMissions(), { initialValue: [] });
  readonly anomalies = toSignal(this.dashboardService.getAnomalies(), { initialValue: [] });

  kpiIcon(id: string): typeof Briefcase {
    return this.kpiIconMap[id] ?? this.icons.Briefcase;
  }

  // --- Graphique barres (missions planifiées vs réalisées) ---
  readonly weeklyMaxValue = computed(() => {
    const points = this.weeklyMissions();
    return Math.max(1, ...points.map((p) => p.planned));
  });

  barHeight(value: number): number {
    return Math.round((value / this.weeklyMaxValue()) * 100);
  }

  // --- Graphique ligne (progression cumulée des installations) ---
  readonly progressPath = computed(() => {
    const points = this.installationProgress();
    if (!points.length) return { line: '', area: '' };

    const max = Math.max(...points.map((p) => p.value));
    const stepX = 100 / (points.length - 1 || 1);

    const coords = points.map((p, i) => {
      const x = i * stepX;
      const y = 100 - (p.value / max) * 100;
      return { x, y };
    });

    const line = coords.map((c, i) => `${i === 0 ? 'M' : 'L'} ${c.x} ${c.y}`).join(' ');
    const area = `${line} L 100 100 L 0 100 Z`;

    return { line, area };
  });

  // --- Donut (répartition du matériel) ---
  readonly materialTotal = computed(() =>
    this.materialDistribution().reduce((sum, item) => sum + item.value, 0)
  );

  readonly donutGradient = computed(() => {
    const items = this.materialDistribution();
    const total = this.materialTotal();
    if (!total) return 'conic-gradient(var(--border-color) 0deg 360deg)';

    let cursor = 0;
    const stops = items.map((item) => {
      const start = (cursor / total) * 360;
      cursor += item.value;
      const end = (cursor / total) * 360;
      return `${item.color} ${start}deg ${end}deg`;
    });

    return `conic-gradient(${stops.join(', ')})`;
  });

  materialPercent(value: number): number {
    const total = this.materialTotal();
    return total ? Math.round((value / total) * 100) : 0;
  }
}