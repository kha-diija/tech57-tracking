import { Routes } from '@angular/router';
import { Auth } from './features/auth/auth';
import { AppShell } from './layout/app-shell/app-shell';
import { authGuard } from './shared/guards/auth.guard';
import { roleGuard } from './shared/guards/role.guard';

export const routes: Routes = [
  // --- Route publique : PAS de sidebar/navbar ---
  { path: 'login', component: Auth },

  // --- Toutes les routes authentifiées passent par le shell (sidebar+navbar) ---
  {
    path: '',
    component: AppShell,
    canActivate: [authGuard],
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
      {
        path: 'dashboard',
        loadComponent: () =>
          import('./features/dashboard/dashboard').then((m) => m.Dashboard),
      },
      {
        path: 'chat-ia',
        loadComponent: () =>
          import('./features/chat-ia/chat-ia').then((m) => m.ChatIa),
      },
      // {
      //   path: 'admin',
      //   canActivate: [roleGuard(['ADMINISTRATEUR'])],
      //   loadComponent: () => import('./features/admin/admin-dashboard').then(m => m.AdminDashboard),
      // },
    ],
  },

  { path: '**', redirectTo: 'login' },
];