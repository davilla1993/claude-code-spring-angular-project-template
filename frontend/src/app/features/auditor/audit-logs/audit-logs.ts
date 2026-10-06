import { DatePipe } from '@angular/common';
import { Component, inject, OnInit, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule } from '@angular/forms';
import { AuditApi } from '../../../core/api/audit-api';
import { apiErrorMessage } from '../../../core/utils/api-error';
import { Pagination } from '../../../shared/components/pagination/pagination';
import { DEFAULT_PAGE_SIZE } from '../../../shared/constants/app.constants';
import { PageResponse } from '../../../shared/models/api-response.model';
import { AuditLog, AuditLogFilters, AuditStatus } from '../../../shared/models/audit-log.model';

/** Consultation filtrable du journal d'audit (permission audit:log:view). */
@Component({
  selector: 'app-audit-logs',
  imports: [ReactiveFormsModule, DatePipe, Pagination],
  templateUrl: './audit-logs.html',
  styleUrl: './audit-logs.scss',
})
export class AuditLogs implements OnInit {
  private readonly auditApi = inject(AuditApi);

  protected readonly filters = inject(NonNullableFormBuilder).group({
    userEmail: [''],
    entityType: [''],
    status: ['' as AuditStatus | ''],
    from: [''],
    to: [''],
  });
  protected readonly page = signal<PageResponse<AuditLog> | null>(null);
  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);

  /** Filtres appliqués (et non ceux en cours de saisie) : la pagination les réutilise. */
  private appliedFilters: AuditLogFilters = {};

  ngOnInit(): void {
    this.load(0);
  }

  protected applyFilters(): void {
    const { userEmail, entityType, status, from, to } = this.filters.getRawValue();
    this.appliedFilters = { userEmail, entityType, status: status || undefined, from, to };
    this.load(0);
  }

  protected resetFilters(): void {
    this.filters.reset();
    this.applyFilters();
  }

  protected load(pageIndex: number): void {
    this.loading.set(true);
    this.error.set(null);
    this.auditApi.search(this.appliedFilters, { page: pageIndex, size: DEFAULT_PAGE_SIZE }).subscribe({
      next: (page) => {
        this.page.set(page);
        this.loading.set(false);
      },
      error: (err: unknown) => {
        this.error.set(apiErrorMessage(err));
        this.loading.set(false);
      },
    });
  }
}
