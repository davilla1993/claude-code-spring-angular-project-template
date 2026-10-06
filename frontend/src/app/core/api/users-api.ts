import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { ApiResponse, PageRequest, PageResponse } from '../../shared/models/api-response.model';
import { CreateUserPayload, Role, UpdateUserPayload, User } from '../../shared/models/user.model';
import { apiUrl, toParams, unwrap } from './api-url';

/** Endpoints /api/users (compte courant + administration). */
@Injectable({ providedIn: 'root' })
export class UsersApi {
  private readonly http = inject(HttpClient);

  // --- Compte courant ---

  me(): Observable<User> {
    return this.http.get<ApiResponse<User>>(apiUrl('/users/me')).pipe(unwrap());
  }

  changePassword(currentPassword: string, newPassword: string): Observable<void> {
    return this.http
      .put<ApiResponse<void>>(apiUrl('/users/me/change-password'), { currentPassword, newPassword })
      .pipe(unwrap());
  }

  setupPassword(newPassword: string): Observable<void> {
    return this.http.put<ApiResponse<void>>(apiUrl('/users/me/setup-password'), { newPassword }).pipe(unwrap());
  }

  // --- Administration ---

  list(page: PageRequest, role?: Role): Observable<PageResponse<User>> {
    const params = toParams({ page: page.page, size: page.size, sort: page.sort, role });
    return this.http.get<ApiResponse<PageResponse<User>>>(apiUrl('/users'), { params }).pipe(unwrap());
  }

  get(id: string): Observable<User> {
    return this.http.get<ApiResponse<User>>(apiUrl(`/users/${encodeURIComponent(id)}`)).pipe(unwrap());
  }

  create(payload: CreateUserPayload): Observable<User> {
    return this.http.post<ApiResponse<User>>(apiUrl('/users'), payload).pipe(unwrap());
  }

  update(id: string, payload: UpdateUserPayload): Observable<User> {
    return this.http.put<ApiResponse<User>>(apiUrl(`/users/${encodeURIComponent(id)}`), payload).pipe(unwrap());
  }

  setActive(id: string, active: boolean): Observable<void> {
    const action = active ? 'activate' : 'deactivate';
    return this.http
      .patch<ApiResponse<void>>(apiUrl(`/users/${encodeURIComponent(id)}/${action}`), null)
      .pipe(unwrap());
  }

  resetPassword(id: string): Observable<string> {
    return this.http
      .post<ApiResponse<{ temporaryPassword: string }>>(apiUrl(`/users/${encodeURIComponent(id)}/reset-password`), null)
      .pipe(
        unwrap(),
        map((data) => data.temporaryPassword),
      );
  }
}
