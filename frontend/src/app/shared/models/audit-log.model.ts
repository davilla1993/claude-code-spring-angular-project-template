export type AuditStatus = 'SUCCESS' | 'FAILED';

export interface AuditLog {
  id: string;
  userEmail: string | null;
  action: string;
  entityType: string;
  entityId: string | null;
  details: string | null;
  ipAddress: string | null;
  status: AuditStatus;
  errorMessage: string | null;
  actionDate: string;
}

export interface AuditLogFilters {
  userEmail?: string;
  entityType?: string;
  status?: AuditStatus;
  /** Date-heure ISO locale (ex : 2026-01-31T08:00). */
  from?: string;
  to?: string;
}
