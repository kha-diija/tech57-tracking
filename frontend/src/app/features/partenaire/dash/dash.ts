import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { LucideAngularModule, Building2, Users, TrendingUp, MapPin } from 'lucide-angular';
import { PartenaireService } from '../../../shared/services/partenaire.service';
import { PartenaireDashboard } from '../../../shared/models/partenaire.model';

@Component({
  selector: 'app-partenaire-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule, LucideAngularModule],
  templateUrl: './dash.html',
  styleUrl: './dash.scss'
})
export class Dash implements OnInit {
  private readonly partenaireService = inject(PartenaireService);

  readonly icons = { Building2, Users, TrendingUp, MapPin };
  readonly isLoading = signal(true);
  readonly errorMessage = signal<string | null>(null);
  readonly data = signal<PartenaireDashboard | null>(null);

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.isLoading.set(true);
    this.errorMessage.set(null);
    this.partenaireService.getDashboard().subscribe({
      next: (res) => {
        this.data.set(res);
        this.isLoading.set(false);
      },
      error: () => {
        this.errorMessage.set('Impossible de charger le tableau de bord.');
        this.isLoading.set(false);
      }
    });
  }

  topEtablissements() {
    const d = this.data();
    if (!d) return [];
    return [...d.etablissements]
      .sort((a, b) => b.pourcentageAvancement - a.pourcentageAvancement)
      .slice(0, 5);
  }
}