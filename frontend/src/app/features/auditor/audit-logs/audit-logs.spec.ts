import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AuditLog } from '../../../shared/models/audit-log.model';
import { AuditLogs } from './audit-logs';

describe('AuditLogs', () => {
  let fixture: ComponentFixture<AuditLogs>;
  let backend: HttpTestingController;

  const element = () => fixture.nativeElement as HTMLElement;
  const isSearch = (request: { url: string }) => request.url === '/api/audit-logs';

  function anEntry(overrides: Partial<AuditLog>): AuditLog {
    return {
      id: 'log-1',
      userEmail: 'jane@example.com',
      activeProfile: null,
      action: 'LOGIN',
      entityType: 'USER',
      entityId: null,
      details: null,
      ipAddress: null,
      status: 'SUCCESS',
      errorMessage: null,
      actionDate: '2026-01-01T10:00:00',
      ...overrides,
    };
  }

  function answer(entries: AuditLog[]): void {
    backend.expectOne(isSearch).flush({
      success: true,
      data: { content: entries, page: 0, size: 20, totalElements: entries.length, totalPages: 1, last: true },
    });
    fixture.detectChanges();
  }

  const cells = (row: number) =>
    [...element().querySelectorAll('tbody tr')[row].querySelectorAll('td')].map((cell) => cell.textContent?.trim());

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [AuditLogs],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    backend = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(AuditLogs);
    fixture.detectChanges();
  });

  afterEach(() => backend.verify());

  it('shows labels for known codes and the raw code otherwise', () => {
    answer([
      anEntry({ id: 'known', action: 'USER_CREATE', entityType: 'USER', activeProfile: 'ADMIN' }),
      anEntry({ id: 'unknown', action: 'ORDER_SHIP', entityType: 'ORDER', activeProfile: 'MANAGER' }),
    ]);

    expect(cells(0).slice(2, 5)).toEqual(['Administrateur', "Création d'un utilisateur", 'Utilisateur']);
    expect(cells(1).slice(2, 5)).toEqual(['MANAGER', 'ORDER_SHIP', 'ORDER']);
  });

  it('filters by entity type, and sends none for « Tous »', () => {
    answer([]);
    const select = element().querySelector<HTMLSelectElement>('#audit-entity')!;
    const submit = () => element().querySelector('form')!.dispatchEvent(new Event('submit'));

    select.value = 'USER';
    select.dispatchEvent(new Event('change'));
    submit();
    const filtered = backend.expectOne(isSearch);
    expect(filtered.request.params.get('entityType')).toBe('USER');
    filtered.flush({ success: true, data: { content: [], page: 0, size: 20, totalElements: 0, totalPages: 0, last: true } });

    select.value = '';
    select.dispatchEvent(new Event('change'));
    submit();
    const all = backend.expectOne(isSearch);
    expect(all.request.params.has('entityType')).toBe(false);
    all.flush({ success: true, data: { content: [], page: 0, size: 20, totalElements: 0, totalPages: 0, last: true } });
  });
});
