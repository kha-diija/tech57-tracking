import { Component, computed, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { toSignal } from '@angular/core/rxjs-interop';
import { LucideAngularModule, Video, BookOpen, FileText, Boxes } from 'lucide-angular';
import { AuthService } from '../../../shared/services/auth.service';
import { ObservateurService } from '../../../shared/services/observateur.service';

@Component({
  selector: 'app-observateur-dashboard',
  standalone: true,
  imports: [CommonModule, LucideAngularModule],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.scss'
})
export class ObservateurDashboard {
  private readonly authService = inject(AuthService);
  private readonly observateurService = inject(ObservateurService);

  readonly icons = { Video, BookOpen, FileText, Boxes };

  readonly prenomObservateur = computed(() => {
    const user = this.authService.currentUser();
    return user?.prenom || user?.nom || 'Observateur';
  });

  readonly summary = toSignal(this.observateurService.getDashboardSummary(), {
    initialValue: { totalVideos: 0, totalRessources: 0, totalDocuments: 0, totalElements: 0, dernierAssignation: null }
  });

  readonly distribution = toSignal(this.observateurService.getDistribution(), { initialValue: [] });
  readonly timeline = toSignal(this.observateurService.getTimeline(), { initialValue: [] });
  readonly mesVideos = toSignal(this.observateurService.getMesVideos(), { initialValue: [] });
  readonly mesRessources = toSignal(this.observateurService.getMesRessources(), { initialValue: [] });
  readonly mesDocuments = toSignal(this.observateurService.getMesDocuments(), { initialValue: [] });

  readonly recentActivity = computed(() => {
    const items = [
      ...this.mesVideos().map(v => ({ label: v.titreVideo, type: 'Vidéo', date: v.dateAssignation, par: v.assigneParAdminNom })),
      ...this.mesRessources().map(r => ({ label: r.titreRessource, type: 'Ressource', date: r.dateAssignation, par: r.assigneParAdminNom })),
      ...this.mesDocuments().map(d => ({ label: d.nomFichier, type: 'Document', date: d.dateAssignation, par: d.assigneParAdminNom })),
    ];
    return items.sort((a, b) => new Date(b.date).getTime() - new Date(a.date).getTime()).slice(0, 6);
  });

  readonly distributionTotal = computed(() => this.distribution().reduce((acc, i) => acc + i.value, 0));

  distributionPercent(value: number): number {
    const total = this.distributionTotal();
    return total ? Math.round((value / total) * 100) : 0;
  }

  readonly donutGradient = computed(() => {
    const items = this.distribution();
    const total = this.distributionTotal();
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

  readonly maxTimelineValue = computed(() => {
    const points = this.timeline();
    if (!points.length) return 1;
    const max = Math.max(...points.map(p => p.total));
    return max === 0 ? 1 : max;
  });

  barHeight(value: number): number {
    return Math.round((value / this.maxTimelineValue()) * 100);
  }

  readonly cumulativePath = computed(() => {
    const points = this.timeline();
    if (!points.length) return { line: '', area: '', coords: [] as any[] };

    let running = 0;
    const cumulPoints = points.map(p => { running += p.total; return { periode: p.periode, value: running }; });

    const values = cumulPoints.map(p => p.value);
    const minVal = Math.min(...values);
    const maxVal = Math.max(...values);
    const range = maxVal - minVal === 0 ? 1 : maxVal - minVal;

    const coords = cumulPoints.map((p, index) => {
      const x = (index / Math.max(cumulPoints.length - 1, 1)) * 100;
      const y = 85 - ((p.value - minVal) / range) * 70;
      return { periode: p.periode, value: p.value, x, y };
    });

    const linePoints = coords.map(c => `${c.x},${c.y}`).join(' L ');
    const line = `M ${linePoints}`;
    const area = `M ${coords[0].x},100 L ${linePoints} L ${coords[coords.length - 1].x},100 Z`;

    return { line, area, coords };
  });
}