import { Component, inject } from '@angular/core';
import { AuthService } from '../../shared/services/auth.service';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  template: `<h1>Bonjour Administrateur {{ authService.currentUser()?.prenom }} 👋</h1>`,
})
export class AdminDashboard {
  authService = inject(AuthService);
}