import { HttpErrorResponse, HttpInterceptorFn, HttpStatusCode } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, switchMap, throwError } from 'rxjs';
import { apiUrl } from '../api/api-url';
import { SessionStore } from '../services/session-store';
import { TokenRefresher } from '../services/token-refresher';

/**
 * Sur 401 : tente un refresh (unique, partagé entre requêtes concurrentes) puis rejoue la requête.
 * Si le refresh échoue, la session est vidée et l'utilisateur renvoyé vers la connexion.
 * Les endpoints /auth/* ne déclenchent jamais de refresh (un 401 au login est une vraie erreur).
 */
export const authRefreshInterceptor: HttpInterceptorFn = (req, next) => {
  const refresher = inject(TokenRefresher);
  const session = inject(SessionStore);
  const router = inject(Router);

  return next(req).pipe(
    catchError((error: unknown) => {
      if (!isUnauthorized(error) || req.url.startsWith(apiUrl('/auth/'))) {
        return throwError(() => error);
      }

      return refresher.refresh().pipe(
        catchError(() => {
          const wasAuthenticated = session.isAuthenticated();
          session.clear();
          if (wasAuthenticated) {
            void router.navigate(['/auth/login'], { queryParams: { returnUrl: router.url } });
          }
          return throwError(() => error);
        }),
        switchMap(() => next(req)),
      );
    }),
  );
};

function isUnauthorized(error: unknown): boolean {
  return error instanceof HttpErrorResponse && error.status === HttpStatusCode.Unauthorized;
}
