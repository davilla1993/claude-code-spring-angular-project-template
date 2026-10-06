import { HttpErrorResponse } from '@angular/common/http';
import { apiErrorData, apiErrorMessage } from './api-error';
import { safeRedirect } from './safe-redirect';

describe('safeRedirect', () => {
  it('keeps internal paths', () => {
    expect(safeRedirect('/app/account?tab=1')).toBe('/app/account?tab=1');
  });

  it.each([undefined, null, '', 'https://evil.com', '//evil.com', '/\\evil.com', 'javascript:alert(1)'])(
    'falls back for unsafe value %s',
    (value) => {
      expect(safeRedirect(value)).toBe('/app');
    },
  );
});

describe('apiErrorMessage', () => {
  it('uses the backend message and validation details', () => {
    const error = new HttpErrorResponse({
      status: 400,
      error: { success: false, message: 'Erreur de validation.', errors: ['email : invalide'] },
    });
    expect(apiErrorMessage(error)).toBe('Erreur de validation. email : invalide');
  });

  it('reports network failures explicitly', () => {
    expect(apiErrorMessage(new HttpErrorResponse({ status: 0 }))).toContain('injoignable');
  });

  it('falls back to a generic message', () => {
    expect(apiErrorMessage(new Error('boom'))).toContain('inattendue');
    expect(apiErrorMessage(new HttpErrorResponse({ status: 500, error: 'html page' }))).toContain('inattendue');
  });
});

describe('apiErrorData', () => {
  it('extracts the data payload of an error response', () => {
    const error = new HttpErrorResponse({ status: 403, error: { success: false, data: { userId: 'u-1' } } });
    expect(apiErrorData<{ userId: string }>(error)?.userId).toBe('u-1');
  });
});
