import { HttpParams } from '@angular/common/http';
import { map, OperatorFunction } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../../shared/models/api-response.model';

export function apiUrl(path: string): string {
  return `${environment.apiBaseUrl}${path}`;
}

/** Extrait `data` de l'enveloppe ApiResponse. */
export function unwrap<T>(): OperatorFunction<ApiResponse<T>, T> {
  return map((response) => response.data as T);
}

/** Construit des HttpParams en ignorant les valeurs vides. */
export function toParams(values: Record<string, string | number | undefined | null>): HttpParams {
  let params = new HttpParams();
  for (const [key, value] of Object.entries(values)) {
    if (value !== undefined && value !== null && value !== '') {
      params = params.set(key, String(value));
    }
  }
  return params;
}
