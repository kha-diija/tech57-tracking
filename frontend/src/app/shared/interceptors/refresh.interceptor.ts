import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, switchMap, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';

export const refreshInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);

  // Ne pas intercepter les appels d'auth eux-mêmes (évite les boucles)
  if (req.url.includes('/auth/login') || req.url.includes('/auth/refresh')) {
    return next(req);
  }

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401 && authService.getRefreshToken()) {
        return authService.refreshToken().pipe(
          switchMap(() => {
            // Rejoue la requête originale avec le nouveau token
            const newToken = authService.getAccessToken();
            const retriedReq = req.clone({
              setHeaders: { Authorization: `Bearer ${newToken}` },
            });
            return next(retriedReq);
          }),
          catchError((refreshError) => {
            // Le refresh token est aussi invalide/expiré → déconnexion forcée
            authService.logout();
            return throwError(() => refreshError);
          })
        );
      }
      return throwError(() => error);
    })
  );
};