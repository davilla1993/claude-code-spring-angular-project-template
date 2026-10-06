import { HttpInterceptorFn } from '@angular/common/http';
import { environment } from '../../../environments/environment';

/**
 * Envoie les cookies d'authentification vers l'API, y compris si elle est servie
 * depuis un sous-domaine du même site.
 */
export const credentialsInterceptor: HttpInterceptorFn = (req, next) =>
  req.url.startsWith(environment.apiBaseUrl) ? next(req.clone({ withCredentials: true })) : next(req);
