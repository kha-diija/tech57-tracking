import { HttpErrorResponse, HttpInterceptorFn, HttpRequest } from '@angular/common/http';
import { inject } from '@angular/core';
import { BehaviorSubject, catchError, filter, switchMap, take, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';

/**
 * Ajoute automatiquement le header Authorization: Bearer <token> sur toutes
 * les requêtes vers l'API backend, et gère le renouvellement automatique
 * de l'access token via le refresh token quand il expire (401).
 */

// Indique si un refresh est déjà en cours (évite d'appeler /refresh en double
// si plusieurs requêtes échouent en 401 en même temps).
let isRefreshing = false;

// Diffuse le nouvel access token à toutes les requêtes qui attendaient
// pendant qu'un refresh était en cours. null = refresh pas encore terminé.
const refreshTokenSubject = new BehaviorSubject<string | null>(null);

export const jwtInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);

  // Ne pas attacher le token, ni tenter de refresh, sur les routes d'auth
  // elles-mêmes (login/refresh) pour éviter les boucles infinies.
  const isAuthRoute = req.url.includes('/auth/login') || req.url.includes('/auth/refresh');

  const authReq = addToken(req, authService.getAccessToken());

  return next(authReq).pipe(
    catchError((error: HttpErrorResponse) => {
      // On ne tente le refresh que sur un 401, et jamais sur les routes d'auth
      // elles-mêmes (sinon un refresh token invalide boucle indéfiniment).
      if (error.status !== 401 || isAuthRoute) {
        return throwError(() => error);
      }

      return handle401Error(authReq, next, authService);
    })
  );
};

function addToken(req: HttpRequest<unknown>, token: string | null): HttpRequest<unknown> {
  if (!token) {
    return req;
  }
  return req.clone({
    setHeaders: { Authorization: `Bearer ${token}` },
  });
}

function handle401Error(
  req: HttpRequest<unknown>,
  next: Parameters<HttpInterceptorFn>[1],
  authService: AuthService
) {
  if (!isRefreshing) {
    isRefreshing = true;
    refreshTokenSubject.next(null);

    const refreshToken = authService.getRefreshToken();

    // APRÈS — passe un message explicite dans les 2 cas d'échec
if (!refreshToken) {
  isRefreshing = false;
  authService.logout('Votre session a expiré. Veuillez vous reconnecter.');
  return throwError(() => new Error('Aucun refresh token disponible'));
}

return authService.refreshToken().pipe(
  switchMap((response) => {
    isRefreshing = false;
    refreshTokenSubject.next(response.accessToken);
    return next(addToken(req, response.accessToken));
  }),
  catchError((refreshError) => {
    isRefreshing = false;
    const message =
      refreshError instanceof HttpErrorResponse && refreshError.error?.message
        ? refreshError.error.message
        : 'Votre session a expiré. Veuillez vous reconnecter.';
    authService.logout(message);
    return throwError(() => refreshError);
  })
);
  }

  // Un refresh est déjà en cours (déclenché par une autre requête en
  // parallèle) : on attend qu'il se termine, puis on rejoue cette requête
  // avec le token fraîchement obtenu, au lieu d'appeler /refresh nous-mêmes.
  return refreshTokenSubject.pipe(
    filter((token): token is string => token !== null),
    take(1),
    switchMap((token) => next(addToken(req, token)))
  );
}