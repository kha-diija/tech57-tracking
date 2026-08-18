import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { toSignal } from '@angular/core/rxjs-interop';
import { LucideAngularModule, Video, BookOpen, FileText } from 'lucide-angular';
import { ObservateurService } from '../../../shared/services/observateur.service';

type TabKey = 'videos' | 'ressources' | 'documents';

@Component({
  selector: 'app-observateur-lecture',
  standalone: true,
  imports: [CommonModule, LucideAngularModule],
  templateUrl: './lecture.html',
  styleUrl: './lecture.scss'
})
export class ObservateurLecture {
  private readonly observateurService = inject(ObservateurService);

  readonly icons = { Video, BookOpen, FileText };
  readonly activeTab = signal<TabKey>('videos');

  readonly mesVideos = toSignal(this.observateurService.getMesVideos(), { initialValue: [] });
  readonly mesRessources = toSignal(this.observateurService.getMesRessources(), { initialValue: [] });
  readonly mesDocuments = toSignal(this.observateurService.getMesDocuments(), { initialValue: [] });

  setTab(tab: TabKey) {
    this.activeTab.set(tab);
  }
}