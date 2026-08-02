import { Routes } from '@angular/router';
import { Auth } from './features/auth/auth';
import { AppShell } from './layout/app-shell/app-shell';
import { authGuard } from './shared/guards/auth.guard';
import { roleGuard } from './shared/guards/role.guard';

// Imports des composants
import { Dashboard } from './features/dashboard/dashboard';
import { Etablissements } from './features/etablissements/etablissements';
import { Missions } from './features/missions/missions';
import { Stock } from './features/stock/stock';
import { Users } from './features/users/users';
import { ChatIa } from './features/chat-ia/chat-ia';
import { Guides } from './features/guides/guides';
import { Settings } from './features/settings/settings';

export const routes: Routes = [
  // ─────────────────────────────────────────────
  // 0. Redirection racine → login
  // ─────────────────────────────────────────────
  { path: '', pathMatch: 'full', redirectTo: 'login' },

  // ─────────────────────────────────────────────
  // 1. Route publique (sans sidebar/navbar)
  // ─────────────────────────────────────────────
  { path: 'login', component: Auth },

  // ─────────────────────────────────────────────
  // 2. Routes protégées sous AppShell (avec sidebar/navbar)
  // ─────────────────────────────────────────────
  {
    path: '',
    component: AppShell,
    canActivate: [authGuard],
    children: [
      { path: 'dashboard', component: Dashboard },
      { path: 'etablissements', component: Etablissements },
      { path: 'missions', component: Missions },
      {
        path: 'simulateur-trajet',
        canActivate: [roleGuard(['TECHNICIEN', 'ADMINISTRATEUR'])],
        loadComponent: () =>
          import('./features/simulateur-trajet/simulateur-trajet').then(m => m.SimulateurTrajet),
      },
      { path: 'stock', component: Stock },
      { path: 'users', component: Users },
      { path: 'chat-ia', component: ChatIa },
      { path: 'guides', component: Guides },
      { path: 'settings', component: Settings },

      // ─── Dashboards spécifiques par rôle ───
      {
        path: 'admin/dashboard',
        canActivate: [roleGuard(['ADMINISTRATEUR'])],
        loadComponent: () =>
          import('./features/admin/admin-dashboard').then(m => m.AdminDashboard),
      },
      {
        path: 'technicien/dashboard',
        canActivate: [roleGuard(['TECHNICIEN'])],
        loadComponent: () =>
          import('./features/technicien/technicien-dashboard').then(m => m.TechnicienDashboard),
      },
      {
        path: 'client/dashboard',
        canActivate: [roleGuard(['OBSERVATEUR'])],
        loadComponent: () =>
          import('./features/client/client-dashboard').then(m => m.ClientDashboard),
      },
      {
        path: 'gestionnaire/dashboard',
        canActivate: [roleGuard(['GESTIONNAIRE_STOCK'])],
        loadComponent: () =>
          import('./features/gestionnaire/gestionnaire-dashboard').then(m => m.GestionnaireDashboard),
      },
    ],
  },

  // ─────────────────────────────────────────────
  // Fallback : route inconnue → login
  // ─────────────────────────────────────────────
  { path: '**', redirectTo: 'login' },
];