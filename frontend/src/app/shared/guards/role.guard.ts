import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { UserRole } from '../models/auth.model';

/**
 * Usage dans app.routes.ts :
 *   {
 *     path: 'admin',
 *     canActivate: [authGuard, roleGuard(['ADMINISTRATEUR'])],
 *     ...
 *   }
 */
export function roleGuard(allowedRoles: UserRole[]): CanActivateFn {
  return () => {
    const authService = inject(AuthService);
    const router = inject(Router);

    if (authService.hasRole(...allowedRoles)) {
      return true;
    }

    // replaceUrl: true => évite d'empiler une entrée d'historique
    // vers une route protégée à laquelle l'utilisateur n'a pas accès
    router.navigateByUrl('/login', { replaceUrl: true });
    return false;
  };
}
