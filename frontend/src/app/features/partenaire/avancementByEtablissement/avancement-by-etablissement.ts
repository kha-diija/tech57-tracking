import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PartenaireService } from '../../../shared/services/partenaire.service';
import { EtablissementAvancement } from '../../../shared/models/partenaire.model';

@Component({
  selector: 'app-avancement-by-etablissement',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './avancement-by-etablissement.html',
  styleUrl: './avancement-by-etablissement.scss'
})
export class AvancementByEtablissement implements OnInit {
  private readonly partenaireService = inject(PartenaireService);

  readonly isLoading = signal(true);
  readonly errorMessage = signal<string | null>(null);
  readonly etablissements = signal<EtablissementAvancement[]>([]);
  readonly nomProvince = signal<string>('');

  ngOnInit(): void {
    this.partenaireService.getDashboard().subscribe({
      next: (res) => {
        this.etablissements.set(res.etablissements);
        this.nomProvince.set(res.nomProvince);
        this.isLoading.set(false);
      },
      error: () => {
        this.errorMessage.set('Impossible de charger la liste des établissements.');
        this.isLoading.set(false);
      }
    });
  }
}