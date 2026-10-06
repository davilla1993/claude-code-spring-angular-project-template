import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiResponse, PageRequest, PageResponse } from '../../shared/models/api-response.model';
import { AuditLog, AuditLogFilters } from '../../shared/models/audit-log.model';
import { apiUrl, toParams, unwrap } from './api-url';

@Injectable({ providedIn: 'root' })
export class AuditApi {
  private readonly http = inject(HttpClient);

  search(filters: AuditLogFilters, page: PageRequest): Observable<PageResponse<AuditLog>> {
    const params = toParams({ ...filters, page: page.page, size: page.size, sort: page.sort });
    return this.http.get<ApiResponse<PageResponse<AuditLog>>>(apiUrl('/audit-logs'), { params }).pipe(unwrap());
  }
}
