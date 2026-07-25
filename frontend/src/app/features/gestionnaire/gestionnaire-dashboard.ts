import { Component, inject } from '@angular/core';
import { AuthService } from '../../shared/services/auth.service';

@Component({
  selector: 'app-gestionnaire-dashboard',
  standalone: true,
  template: `<h1>Bonjour Gestionnaire {{ authService.currentUser()?.prenom }} 👋</h1>`,
})
export class GestionnaireDashboard {
  authService = inject(AuthService);
}