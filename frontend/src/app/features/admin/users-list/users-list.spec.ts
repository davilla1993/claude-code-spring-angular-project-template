import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting, TestRequest } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { SessionStore } from '../../../core/services/session-store';
import { PageResponse } from '../../../shared/models/api-response.model';
import { User } from '../../../shared/models/user.model';
import { aUser } from '../../../testing/test-data';
import { UsersList } from './users-list';

describe('UsersList filters', () => {
  let fixture: ComponentFixture<UsersList>;
  let backend: HttpTestingController;

  const element = () => fixture.nativeElement as HTMLElement;
  const isList = (request: { url: string }) => request.url === '/api/users';

  function pageOf(page: number, totalPages: number): PageResponse<User> {
    return { content: [aUser()], page, size: 20, totalElements: totalPages * 20, totalPages, last: page === totalPages - 1 };
  }

  async function answer(request: TestRequest, page = 0, totalPages = 1): Promise<void> {
    request.flush({ success: true, data: pageOf(page, totalPages) });
    fixture.detectChanges();
    await fixture.whenStable();
  }

  async function selectRole(role: string): Promise<void> {
    const select = element().querySelector<HTMLSelectElement>('#users-role-filter')!;
    select.value = role;
    select.dispatchEvent(new Event('change'));
    fixture.detectChanges();
    await fixture.whenStable();
  }

  const clickButton = (label: string) =>
    [...element().querySelectorAll('button')].find((button) => button.textContent?.trim() === label)!.click();

  beforeEach(async () => {
    TestBed.configureTestingModule({
      imports: [UsersList],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    });
    backend = TestBed.inject(HttpTestingController);
    TestBed.inject(SessionStore).setUser(aUser({ publicId: 'me', activeRole: 'ADMIN' }));
    fixture = TestBed.createComponent(UsersList);
    fixture.detectChanges();
    await fixture.whenStable();
  });

  afterEach(() => backend.verify());

  it('does not reload the list when a role is only selected', async () => {
    await answer(backend.expectOne(isList));

    await selectRole('ADMIN');

    backend.expectNone(isList);
  });

  it('applies the selected role with the Filtrer button', async () => {
    await answer(backend.expectOne(isList));
    await selectRole('ADMIN');

    clickButton('Filtrer');

    const request = backend.expectOne(isList);
    expect(request.request.params.get('role')).toBe('ADMIN');
    expect(request.request.params.get('page')).toBe('0');
    await answer(request);
  });

  it('keeps the applied role, not the one being selected, when changing page', async () => {
    await answer(backend.expectOne(isList));
    await selectRole('ADMIN');
    clickButton('Filtrer');
    await answer(backend.expectOne(isList), 0, 2);

    await selectRole('USER'); // sélection non appliquée
    clickButton('Suivant');

    const request = backend.expectOne(isList);
    expect(request.request.params.get('role')).toBe('ADMIN');
    expect(request.request.params.get('page')).toBe('1');
    await answer(request, 1, 2);
  });

  it('reloads without any role with the Réinitialiser button', async () => {
    await answer(backend.expectOne(isList));
    await selectRole('ADMIN');
    clickButton('Filtrer');
    await answer(backend.expectOne(isList));

    clickButton('Réinitialiser');

    const request = backend.expectOne(isList);
    expect(request.request.params.has('role')).toBe(false);
    await answer(request);
  });
});
