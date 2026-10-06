import { registerLocaleData } from '@angular/common';
import { provideHttpClient, withFetch, withInterceptors } from '@angular/common/http';
import localeFr from '@angular/common/locales/fr';
import {
  ApplicationConfig,
  inject,
  LOCALE_ID,
  provideAppInitializer,
  provideBrowserGlobalErrorListeners,
} from '@angular/core';
import { provideRouter, withComponentInputBinding } from '@angular/router';

import { routes } from './app.routes';
import { authRefreshInterceptor } from './core/interceptors/auth-refresh-interceptor';
import { credentialsInterceptor } from './core/interceptors/credentials-interceptor';
import { SessionStore } from './core/services/session-store';

registerLocaleData(localeFr);

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    { provide: LOCALE_ID, useValue: 'fr-FR' },
    provideHttpClient(withFetch(), withInterceptors([credentialsInterceptor, authRefreshInterceptor])),
    provideRouter(routes, withComponentInputBinding()),
    // La session est restaurée avant la première navigation : les guards voient l'état réel.
    provideAppInitializer(() => inject(SessionStore).restore()),
  ],
};
