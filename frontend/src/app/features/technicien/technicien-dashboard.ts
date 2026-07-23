import { Component, inject } from '@angular/core';
import { AuthService } from '../../shared/services/auth.service';

@Component({
  selector: 'app-technicien-dashboard',
  standalone: true,
  template: `<h1>Bonjour Technicien {{ authService.currentUser()?.prenom }} 👋</h1>`,
})
export class TechnicienDashboard {
  authService = inject(AuthService);
}