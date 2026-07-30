import { Component, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { LucideAngularModule, Briefcase, Plus, Search, Calendar, Users, Trash2, CheckCircle2, Clock } from 'lucide-angular';
import { MissionService } from '../../../../shared/services/mission.service';
import { Mission, MissionKpi, MissionPayload } from '../../../../shared/models/mission.model';

@Component({
  selector: 'app-gs-mission',
  standalone: true,
  imports: [CommonModule, FormsModule, LucideAngularModule],
  templateUrl: './gs-mission.html',
  styleUrl: './gs-mission.scss'
})
export class GsMission {
  private readonly missionService = inject(MissionService);

  readonly icons = { Briefcase, Plus, Search, Calendar, Users, Trash2, CheckCircle2, Clock };

  readonly missions = signal<Mission[]>([]);
  readonly kpis = signal<MissionKpi | null>(null);
  readonly searchTerm = signal<string>('');

  // Formulaire modal / panneau latéral
  readonly showForm = signal<boolean>(false);
  readonly formModel = signal<MissionPayload>({
    reference: 'MSN-2026-' + Math.floor(100 + Math.random() * 900),
    titre: '',
    etablissementNom: '',
    equipeAffectee: '',
    datePrevue: '',
    priorite: 'NORMALE',
    statut: 'VALIDEE',
    budgetEstime: 0
  });

  readonly filteredMissions = computed(() => {
    const term = this.searchTerm().trim().toLowerCase();
    return this.missions().filter(m =>
      !term ||
      m.titre.toLowerCase().includes(term) ||
      m.reference.toLowerCase().includes(term) ||
      m.etablissementNom.toLowerCase().includes(term) ||
      m.equipeAffectee.toLowerCase().includes(term)
    );
  });

  constructor() {
    this.loadData();
  }

  private loadData(): void {
    this.missionService.getAll().subscribe(data => this.missions.set(data));
    this.missionService.getKpis().subscribe(data => this.kpis.set(data));
  }

  openCreateForm(): void {
    this.formModel.set({
      reference: 'MSN-2026-' + Math.floor(100 + Math.random() * 900),
      titre: '',
      etablissementNom: '',
      equipeAffectee: '',
      datePrevue: '',
      priorite: 'NORMALE',
      statut: 'VALIDEE',
      budgetEstime: 0
    });
    this.showForm.set(true);
  }

  closeForm(): void {
    this.showForm.set(false);
  }

  saveForm(): void {
    const m = this.formModel();
    if (!m.titre.trim() || !m.etablissementNom.trim()) return;

    this.missionService.create(m).subscribe(() => {
      this.closeForm();
      this.loadData();
    });
  }

  deleteMission(item: Mission): void {
    if (!confirm(`Supprimer la mission "${item.reference}" ?`)) return;
    this.missionService.delete(item.idMission).subscribe(() => this.loadData());
  }
}