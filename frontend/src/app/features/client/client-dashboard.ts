import { Component, inject } from '@angular/core';
import { AuthService } from '../../shared/services/auth.service';

@Component({
  selector: 'app-client-dashboard',
  standalone: true,
  template: `<h1>Bonjour Client {{ authService.currentUser()?.prenom }} 👋</h1>`,
})
export class ClientDashboard {
  authService = inject(AuthService);
}