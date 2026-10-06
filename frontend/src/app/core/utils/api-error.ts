import { HttpErrorResponse } from '@angular/common/http';
import { ApiResponse } from '../../shared/models/api-response.model';

const NETWORK_ERROR = 'Le serveur est injoignable. Vérifiez votre connexion puis réessayez.';
const UNKNOWN_ERROR = 'Une erreur inattendue est survenue. Veuillez réessayer.';

/** Message lisible à afficher pour une erreur d'appel API (message du backend si disponible). */
export function apiErrorMessage(error: unknown): string {
  if (!(error instanceof HttpErrorResponse)) {
    return UNKNOWN_ERROR;
  }
  if (error.status === 0) {
    return NETWORK_ERROR;
  }
  const body = error.error as ApiResponse<unknown> | null;
  const details = body?.errors?.length ? ` ${body.errors.join(' ')}` : '';
  return body?.message ? body.message + details : UNKNOWN_ERROR;
}

/** Données associées à une erreur API (ex : { userId } quand l'email n'est pas vérifié). */
export function apiErrorData<T>(error: unknown): T | undefined {
  return error instanceof HttpErrorResponse ? ((error.error as ApiResponse<T> | null)?.data ?? undefined) : undefined;
}
