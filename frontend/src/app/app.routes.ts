import { Routes } from '@angular/router';
import { Dashboard } from './features/dashboard/dashboard';
import { Auth } from './features/auth/auth';
import { Missions } from './features/missions/missions';
import { Etablissements } from './features/etablissements/etablissements';
import { ChatIa } from './features/chat-ia/chat-ia';

export const routes: Routes = [
  { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
  { path: 'login', component: Auth },
  { path: 'dashboard', component: Dashboard },
  { path: 'missions', component: Missions },
  { path: 'etablissements', component: Etablissements },
  { path: 'chat-ia', component: ChatIa },
];