import { Routes } from '@angular/router';
import { Auth } from './features/auth/auth';
import { AppShell } from './layout/app-shell/app-shell';
import { authGuard } from './shared/guards/auth.guard';
import { roleGuard } from './shared/guards/role.guard';

// Imports de tes composants
import { Dashboard } from './features/dashboard/dashboard';
import { Missions } from './features/missions/missions';
import { Etablissements } from './features/etablissements/etablissements';
import { ChatIa } from './features/chat-ia/chat-ia';
import { Stock } from './features/stock/stock';
import { Users } from './features/users/users';
import { Guides } from './features/guides/guides';
import { Settings } from './features/settings/settings';

export const routes: Routes = [
  // 1. Route publique : Login (SANS sidebar/navbar)
  { path: 'login', component: Auth },

  // 2. Routes protégées sous AppShell (AVEC sidebar/navbar)
  {
    path: '',
    component: AppShell,
    canActivate: [authGuard],
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
      { path: 'dashboard', component: Dashboard },
      { path: 'missions', component: Missions },
      { path: 'etablissements', component: Etablissements },
      { path: 'chat-ia', component: ChatIa },
      { path: 'stock', component: Stock },
      { path: 'users', component: Users },
      { path: 'guides', component: Guides },
      { path: 'settings', component: Settings },

      // Dashboards spécifiques par rôle (placés ICI dans AppShell)
      {
        path: 'admin/dashboard',
        canActivate: [roleGuard(['ADMINISTRATEUR'])],
        loadComponent: () =>
          import('./features/admin/admin-dashboard').then((m) => m.AdminDashboard),
      },
      {
        path: 'technicien/dashboard',
        canActivate: [roleGuard(['TECHNICIEN'])],
        loadComponent: () =>
          import('./features/technicien/technicien-dashboard').then((m) => m.TechnicienDashboard),
      },
      {
        path: 'client/dashboard',
        canActivate: [roleGuard(['OBSERVATEUR'])],
        loadComponent: () =>
          import('./features/client/client-dashboard').then((m) => m.ClientDashboard),
      },
      {
        path: 'gestionnaire/dashboard',
        canActivate: [roleGuard(['GESTIONNAIRE_STOCK'])],
        loadComponent: () =>
          import('./features/gestionnaire/gestionnaire-dashboard').then((m) => m.GestionnaireDashboard),
      },
    ],
  },

  // Redirection par défaut vers le login pour toute route inconnue
  { path: '**', redirectTo: 'login' },
];