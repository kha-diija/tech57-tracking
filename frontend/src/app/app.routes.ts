import { Routes } from '@angular/router';
import { Dashboard } from './features/dashboard/dashboard';
import { Auth } from './features/auth/auth';
import { Missions } from './features/missions/missions';
import { Etablissements } from './features/etablissements/etablissements';
import { ChatIa } from './features/chat-ia/chat-ia';

// 1. Importe tes composants manquants
import { Stock } from './features/stock/stock';
import { Users } from './features/users/users';
import { Guides } from './features/guides/guides';
import { Settings } from './features/settings/settings';

export const routes: Routes = [
  { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
  { path: 'login', component: Auth },
  { path: 'dashboard', component: Dashboard },
  { path: 'missions', component: Missions },
  { path: 'etablissements', component: Etablissements },
  { path: 'chat-ia', component: ChatIa },

  // 2. Ajoute les routes correspondantes
  { path: 'stock', component: Stock },
  { path: 'users', component: Users },
  { path: 'guides', component: Guides },
  { path: 'settings', component: Settings },

  // Facultatif : Redirection si l'URL saisie n'existe pas
  { path: '**', redirectTo: 'dashboard' }
];