import { ApplicationConfig, provideBrowserGlobalErrorListeners, importProvidersFrom } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { LucideAngularModule, icons } from 'lucide-angular';

import { routes } from './app.routes';
import { jwtInterceptor } from './shared/interceptors/jwt.interceptor';
import { refreshInterceptor } from './shared/interceptors/refresh.interceptor'; // ← AJOUTE ÇA

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes),
    // ⚠️ Ordre important : JWT d'abord, puis REFRESH
    provideHttpClient(withInterceptors([jwtInterceptor, refreshInterceptor])), // ← MODIFIE ÇA
    importProvidersFrom(LucideAngularModule.pick(icons)),
  ]
};