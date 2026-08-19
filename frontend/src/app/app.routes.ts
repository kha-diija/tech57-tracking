import { Routes } from '@angular/router';
import { Auth } from './features/auth/auth';
import { AppShell } from './layout/app-shell/app-shell';
import { authGuard } from './shared/guards/auth.guard';
import { roleGuard } from './shared/guards/role.guard';

// Imports des composants globaux
import { Dashboard } from './features/admin/dashboard';
import { Missions } from './features/missions/missions';
import { Etablissements } from './features/etablissements/etablissements';
import { ChatIa } from './features/chat-ia/chat-ia';
import { Guides } from './features/guides/guides';
import { Stock } from './features/stock/stock';
import { Users } from './features/users/users';
import { Ressources } from './features/admin/ressources/ressources';
import { Interventions } from './features/admin/interventions/interventions';
import { Settings } from './features/settings/settings';


export const routes: Routes = [
  // 0. Redirection par défaut de la racine vers /login
  { path: '', pathMatch: 'full', redirectTo: 'login' },

  // 1. Route publique : Login (SANS sidebar/navbar)
  { path: 'login', component: Auth },

  // 2. Routes protégées sous AppShell (AVEC sidebar/navbar)
  {
    path: '',
    component: AppShell,
    canActivate: [authGuard],
    children: [
      { path: 'dashboard', component: Dashboard },
      { path: 'missions', component: Missions },
      { path: 'etablissements', component: Etablissements },
      {
        path: 'admin/simulateur-trajet',
        canActivate: [roleGuard(['TECHNICIEN', 'ADMINISTRATEUR'])],
        loadComponent: () =>
          import('./features/simulateur-trajet/simulateur-trajet').then(m => m.SimulateurTrajet),
      },
      { path: 'chat-ia', component: ChatIa },
      { path: 'guides', component: Guides },
      // APRÈS
{ 
  path: 'stock', 
  component: Stock, 
  canActivate: [roleGuard(['ADMINISTRATEUR', 'GESTIONNAIRE_STOCK', 'TECHNICIEN'])] 
},

     
      {
        path: 'users',
        component: Users,
        canActivate: [roleGuard(['ADMINISTRATEUR'])]
      },
      { path: 'guides', component: Guides },
      { path: 'ressources', component: Ressources },
      { path: 'interventions', component: Interventions },
      { path: 'settings', component: Settings },

      // Dashboards et modules spécifiques par rôle (avec Lazy Loading)
      {
        path: 'admin/dashboard',
        canActivate: [roleGuard(['ADMINISTRATEUR'])],
        loadComponent: () =>
          import('./features/admin/dashboard').then((m) => m.Dashboard),
      },
      {
        path: 'admin/etablissements',
        canActivate: [roleGuard(['ADMINISTRATEUR' , 'TECHNICIEN'])],
        loadComponent: () =>
          import('./features/admin/Gs-etablissement/gs-etablissement').then((m) => m.GsEtablissement),
      },
      {
        path: 'admin/missions',
        canActivate: [roleGuard(['ADMINISTRATEUR'])],
        loadComponent: () =>
          import('./features/admin/missions/gs-mission/gs-mission').then((m) => m.GsMission),
      },
      {
        path: 'admin/ressources',
        canActivate: [roleGuard(['ADMINISTRATEUR'])],
        loadComponent: () =>
          import('./features/admin/ressources/ressources').then((m) => m.Ressources),
      },
      {
        path: 'admin/interventions',
        canActivate: [roleGuard(['ADMINISTRATEUR'])],
        pathMatch: 'full',
        loadComponent: () =>
          import('./features/admin/interventions/interventions').then((m) => m.Interventions),
      },
      {
        path: 'admin/observateurs',
        canActivate: [roleGuard(['ADMINISTRATEUR'])],
        loadComponent: () =>
          import('./features/observateur/observateur').then((m) => m.ObservateurPermissions),
      },
      {
        path: 'technicien/dashboard',
        canActivate: [roleGuard(['TECHNICIEN'])],
        loadComponent: () =>
          import('./features/technicien/technicien-dashboard').then((m) => m.TechnicienDashboard),
      },
      // {
      //   path: 'client/dashboard',
      //   canActivate: [roleGuard(['OBSERVATEUR'])],
      //   loadComponent: () =>
      //     import('./features/client/client-dashboard').then((m) => m.ClientDashboard),
      // },
      {
        path: 'technicien/missions',
        canActivate: [roleGuard(['TECHNICIEN'])],
        loadComponent: () =>
          import('./features/technicien/technicien-mission').then((m) => m.TechnicienMissionComponent),
      },
      {
        path: 'client/dashboard',
        canActivate: [roleGuard(['OBSERVATEUR'])],
        loadComponent: () =>
          import('./features/role_observateur/dashboard/dashboard').then(m => m.ObservateurDashboard),
      },
      {
        path: 'observateur/ressources',
        canActivate: [roleGuard(['OBSERVATEUR'])],
        loadComponent: () =>
          import('./features/role_observateur/lecture/lecture').then(m => m.ObservateurLecture),
      },
      {
        path: 'gestionnaire/dashboard',
        canActivate: [roleGuard(['GESTIONNAIRE_STOCK'])],
        loadComponent: () =>
          import('./features/gestionnaire/gestionnaire-dashboard').then((m) => m.GestionnaireDashboard),
      },
      {
        path: 'sorties',
        canActivate: [roleGuard(['ADMINISTRATEUR', 'GESTIONNAIRE_STOCK'])],
        loadComponent: () =>
          import('./features/gestionnaire/sorties/sorties').then((m) => m.SortiesStock),
      },
      {
        path: 'retours',
        canActivate: [roleGuard(['ADMINISTRATEUR', 'GESTIONNAIRE_STOCK'])],
        loadComponent: () =>
          import('./features/gestionnaire/retours/retours').then((m) => m.RetoursStock),
      },

    ],
  },

  // Redirection par défaut vers le login pour toute route inconnue
  { path: '**', redirectTo: 'login' },
];
