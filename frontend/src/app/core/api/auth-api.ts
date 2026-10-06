import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiResponse } from '../../shared/models/api-response.model';
import { User } from '../../shared/models/user.model';
import { apiUrl, unwrap } from './api-url';

export interface RegisterPayload {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
}

export interface ResetPasswordPayload {
  email: string;
  code: string;
  newPassword: string;
}

/**
 * Endpoints /api/auth. Les tokens circulent uniquement par cookies HttpOnly :
 * ce service ne les manipule jamais.
 */
@Injectable({ providedIn: 'root' })
export class AuthApi {
  private readonly http = inject(HttpClient);

  login(email: string, password: string): Observable<User> {
    return this.http.post<ApiResponse<User>>(apiUrl('/auth/login'), { email, password }).pipe(unwrap());
  }

  register(payload: RegisterPayload): Observable<User> {
    return this.http.post<ApiResponse<User>>(apiUrl('/auth/register'), payload).pipe(unwrap());
  }

  refresh(): Observable<User> {
    return this.http.post<ApiResponse<User>>(apiUrl('/auth/refresh'), null).pipe(unwrap());
  }

  logout(): Observable<void> {
    return this.http.post<ApiResponse<void>>(apiUrl('/auth/logout'), null).pipe(unwrap());
  }

  verifyEmail(userId: string, code: string): Observable<void> {
    return this.http.post<ApiResponse<void>>(apiUrl('/auth/verify-email'), { userId, code }).pipe(unwrap());
  }

  resendVerification(email: string): Observable<void> {
    return this.http.post<ApiResponse<void>>(apiUrl('/auth/resend-verification'), { email }).pipe(unwrap());
  }

  forgotPassword(email: string): Observable<void> {
    return this.http.post<ApiResponse<void>>(apiUrl('/auth/forgot-password'), { email }).pipe(unwrap());
  }

  resetPassword(payload: ResetPasswordPayload): Observable<void> {
    return this.http.post<ApiResponse<void>>(apiUrl('/auth/reset-password'), payload).pipe(unwrap());
  }
}
