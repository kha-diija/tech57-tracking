import { Component, computed, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { toSignal } from '@angular/core/rxjs-interop';
import {
  LucideAngularModule,
  PackageMinus,
  PackageCheck,
  Wrench,
  ShieldCheck,
  ArrowUpRight,
  ArrowDownRight,
  AlertTriangle
} from 'lucide-angular';
import { AuthService } from '../../shared/services/auth.service';
import { StockDashboardService, StockOutPoint } from '../../shared/services/stock-dashboard.service';

@Component({
  selector: 'app-stock-dashboard',
  standalone: true,
  imports: [CommonModule, LucideAngularModule],
  templateUrl: './stock-dashboard.html',
  styleUrl: './stock-dashboard.scss'
})
export class StockDashboard {
  private readonly authService = inject(AuthService);
  private readonly stockDashboardService = inject(StockDashboardService);

  readonly icons = {
    PackageMinus,
    PackageCheck,
    Wrench,
    ShieldCheck,
    ArrowUpRight,
    ArrowDownRight,
    AlertTriangle
  };

  private readonly kpiIconMap: Record<string, typeof PackageMinus> = {
    sorties: PackageMinus,
    disponible: PackageCheck,
    maintenance: Wrench,
    disponibilite: ShieldCheck
  };

  readonly prenomGestionnaire = computed(() => {
    const user = this.authService.currentUser();
    return user?.prenom || user?.nom || 'Gestionnaire';
  });

  readonly kpis = toSignal(this.stockDashboardService.getKpis(), { initialValue: [] });
  readonly weeklyStockOut = toSignal(this.stockDashboardService.getWeeklyStockOut(), { initialValue: [] as StockOutPoint[] });
  readonly stockDistribution = toSignal(this.stockDashboardService.getStockDistribution(), { initialValue: [] });
  readonly maintenanceList = toSignal(this.stockDashboardService.getMaintenanceList(), { initialValue: [] });
  readonly lowStockAlerts = toSignal(this.stockDashboardService.getLowStockAlerts(), { initialValue: [] });

  kpiIcon(id: string) {
    return this.kpiIconMap[id] || PackageCheck;
  }

  readonly maxStockOutValue = computed(() => {
    const points = this.weeklyStockOut();
    if (!points.length) return 10;
    const max = Math.max(...points.map(p => p.quantite));
    return max === 0 ? 10 : max;
  });

  barHeight(value: number): number {
    return Math.round((value / this.maxStockOutValue()) * 100);
  }

  readonly stockTotal = computed(() =>
    this.stockDistribution().reduce((acc, item) => acc + item.value, 0)
  );

  stockPercent(value: number): number {
    const total = this.stockTotal();
    return total ? Math.round((value / total) * 100) : 0;
  }

  readonly donutGradient = computed(() => {
    const items = this.stockDistribution();
    const total = this.stockTotal();
    if (!total || !items.length) return 'conic-gradient(var(--border-color) 0deg 360deg)';

    let cumulative = 0;
    const stops: string[] = [];
    for (const item of items) {
      const percent = (item.value / total) * 100;
      stops.push(`${item.color} ${cumulative}% ${cumulative + percent}%`);
      cumulative += percent;
    }
    return `conic-gradient(${stops.join(', ')})`;
  });
}
