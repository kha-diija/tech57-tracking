import { Component, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { ElementRef, ViewChild } from '@angular/core';
import html2canvas from 'html2canvas';
import jsPDF from 'jspdf';
import { toSignal, toObservable } from '@angular/core/rxjs-interop';
import { switchMap } from 'rxjs';
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
  FileText,
  Clock
} from 'lucide-angular';
import { AuthService } from '../../shared/services/auth.service';
import { DashboardService, WeeklyMissionPoint, InstallationPoint } from '../../shared/services/Dashboard.service';

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
  private readonly router = inject(Router);

  // Palette de couleurs personnalisée pour le style du donut
  private readonly defaultColors = [
    'rgba(10, 206, 193, 0.95)', // Cyan principal
    '#3b82f6',                 // Bleu vibrant
    '#334155',                 // Gris sombre moderne
    '#a855f7',                 // Violet
    '#f59e0b'                  // Ambre
  ];

  readonly icons = {
    Briefcase,
    Building2,
    FileText,
    Users,
    ShieldCheck,
    ArrowUpRight,
    ArrowDownRight,
    Filter,
    Download,
    Plus,
    CheckCircle2,
    MapPin,
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

  // --- Gestion du Filtre de Période (Réactif) ---
  readonly selectedPeriod = signal<string>('7d');

  readonly kpis = toSignal(
    toObservable(this.selectedPeriod).pipe(
      switchMap(period => this.dashboardService.getKpis(period))
    ),
    { initialValue: [] }
  );

  // --- Autres Données du Dashboard ---
  readonly weeklyMissions = toSignal(this.dashboardService.getWeeklyMissions(), { initialValue: [] });
  readonly installationProgress = toSignal(this.dashboardService.getInstallationProgress(), { initialValue: [] });
  readonly materialDistribution = toSignal(this.dashboardService.getMaterialDistribution(), { initialValue: [] });
  readonly recentActivity = toSignal(this.dashboardService.getRecentActivity(), { initialValue: [] });
  readonly upcomingMissions = toSignal(this.dashboardService.getUpcomingMissions(), { initialValue: [] });

  // --- État des tooltips & interactions ---
  readonly activeWeeklyTooltip = signal<WeeklyMissionPoint | null>(null);
  readonly activeMaterialTooltip = signal<{ label: string; value: number; percent: number } | null>(null);
  readonly activeProgressTooltip = signal<InstallationPoint | null>(null);

  // --- État pour la modale "Tout voir" (Activité récente) ---
  readonly showAllActivityModal = signal<boolean>(false);

  // État de chargement pour l'export
  readonly isExporting = signal<boolean>(false);

    @ViewChild('dashboardContent', { static: false }) dashboardContent!: ElementRef;
  kpiIcon(id: string) {
    return this.kpiIconMap[id] || Briefcase;
  }

  // --- Actions / Événements ---
  onFilter() {
    const nextPeriod = this.selectedPeriod() === '7d' ? '30d' : '7d';
    this.selectedPeriod.set(nextPeriod);
  }

  onExport() {
    this.isExporting.set(true);
    this.dashboardService.exportDashboardData().subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = `dashboard-report-${new Date().toISOString().slice(0, 10)}.csv`;
        link.click();
        window.URL.revokeObjectURL(url);
        this.isExporting.set(false);
      },
      error: (err) => {
        console.error('Erreur lors de l’exportation', err);
        this.isExporting.set(false);
      }
    });
  }

   onExportPdf() {
    this.isExporting.set(true);
    const element = this.dashboardContent.nativeElement;
    
    // ✅ 1. Masquer les boutons temporairement
    const actionButtons = element.querySelectorAll('.dashboard__actions .btn');
    const originalDisplays: string[] = [];
    actionButtons.forEach((btn: any) => {
      originalDisplays.push(btn.style.display);
      btn.style.display = 'none';
    });

    // ✅ 2. Capturer le dashboard SANS les boutons
    html2canvas(element, {
      scale: 2,
      useCORS: true,
      backgroundColor: '#0f1115',
      logging: false
    }).then((canvas) => {
      // ✅ 3. Restaurer les boutons (pour le navigateur)
      actionButtons.forEach((btn: any, index: number) => {
        btn.style.display = originalDisplays[index];
      });
      
      const imgData = canvas.toDataURL('image/png');
      const pdf = new jsPDF('p', 'mm', 'a4');

      const pageWidth = pdf.internal.pageSize.getWidth();
      const pageHeight = pdf.internal.pageSize.getHeight();
      const imgWidth = pageWidth;
      const imgHeight = (canvas.height * imgWidth) / canvas.width;

      let heightLeft = imgHeight;
      let position = 0;

      pdf.addImage(imgData, 'PNG', 0, position, imgWidth, imgHeight);
      heightLeft -= pageHeight;

      while (heightLeft > 0) {
        position = heightLeft - imgHeight;
        pdf.addPage();
        pdf.addImage(imgData, 'PNG', 0, position, imgWidth, imgHeight);
        heightLeft -= pageHeight;
      }

      pdf.save('dashboard.pdf');
      this.isExporting.set(false);
    }).catch((err) => {
      // ✅ Restaurer les boutons en cas d'erreur
      actionButtons.forEach((btn: any, index: number) => {
        btn.style.display = originalDisplays[index];
      });
      console.error('Erreur lors de l’export PDF', err);
      this.isExporting.set(false);
    });
}

  onNewMission() {
    this.router.navigate(['/admin/missions']);
  }

  onViewAllActivity() {
    this.showAllActivityModal.set(true);
  }

  closeActivityModal() {
    this.showAllActivityModal.set(false);
  }

  // --- Helpers Graphiques ---
  readonly maxWeeklyValue = computed(() => {
    const points = this.weeklyMissions();
    if (!points.length) return 10;
    const max = Math.max(...points.map(p => Math.max(p.planned, p.completed)));
    return max === 0 ? 10 : max;
  });

  barHeight(value: number): number {
    const max = this.maxWeeklyValue();
    return Math.round((value / max) * 100);
  }

  dayCompletionRatio(point: WeeklyMissionPoint): number {
    if (!point.planned) return 0;
    return Math.round((point.completed / point.planned) * 100);
  }

  readonly weeklyCompletionRate = computed(() => {
    const points = this.weeklyMissions();
    if (!points.length) return 0;
    const totalPlanned = points.reduce((acc, p) => acc + p.planned, 0);
    const totalCompleted = points.reduce((acc, p) => acc + p.completed, 0);
    if (!totalPlanned) return 0;
    return Math.round((totalCompleted / totalPlanned) * 100);
  });

  readonly materialTotal = computed(() => {
    const items = this.materialDistribution();
    return items.reduce((acc, item) => acc + item.value, 0);
  });

  materialPercent(value: number): number {
    const total = this.materialTotal();
    if (!total) return 0;
    return Math.round((value / total) * 100);
  }

  // --- CALCUL DYNAMIQUE DU DEGRADÉ DU DONUT (AVEC PALETTE CYAN REFORMATEE) ---
  readonly donutGradient = computed(() => {
    const items = this.materialDistribution();
    const total = this.materialTotal();
    if (!total || !items.length) return 'conic-gradient(var(--border-color) 0deg 360deg)';

    let cumulativePercent = 0;
    const stops: string[] = [];

    // Palette forcée : Cyan principal, Bleu, Gris foncé
    const forcedColors = [
      'rgba(10, 206, 193, 0.95)', // Cyan (3412 unités)
      '#3b82f6',                 // Bleu (412 unités)
      '#334155'                  // Gris (890 unités)
    ];

    items.forEach((item, index) => {
      // FORCE LA NOUVELLE COULEUR (écrase item.color d'origine)
      const color = forcedColors[index % forcedColors.length];
      item.color = color; 

      const percent = (item.value / total) * 100;
      const start = cumulativePercent;
      const end = cumulativePercent + percent;
      stops.push(`${color} ${start}% ${end}%`);
      cumulativePercent = end;
    });

    return `conic-gradient(${stops.join(', ')})`;
  });

  readonly progressPath = computed(() => {
    const points = this.installationProgress();
    if (!points.length) return { line: '', area: '', coords: [] };

    const values = points.map(p => p.value);
    const minVal = Math.min(...values);
    const maxVal = Math.max(...values);
    const range = maxVal - minVal === 0 ? 1 : maxVal - minVal;

    const coords = points.map((p, index) => {
      const x = (index / (points.length - 1)) * 100;
      const y = 85 - ((p.value - minVal) / range) * 70;
      return { week: p.week, value: p.value, x, y };
    });

    const linePoints = coords.map(c => `${c.x},${c.y}`).join(' L ');
    const line = `M ${linePoints}`;
    const area = `M ${coords[0].x},100 L ${linePoints} L ${coords[coords.length - 1].x},100 Z`;

    return { line, area, coords };
  });

  readonly progressGrowth = computed(() => {
    const points = this.installationProgress();
    if (points.length < 2) return 0;
    const first = points[0].value;
    const last = points[points.length - 1].value;
    if (!first) return 0;
    return Math.round(((last - first) / first) * 100);
  });
}
