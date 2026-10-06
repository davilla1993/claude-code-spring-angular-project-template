import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { App } from './app';

describe('App', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [App],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();
  });

  it('renders the layout with a skip link targeting the main content', async () => {
    const fixture = TestBed.createComponent(App);
    await fixture.whenStable();
    const element = fixture.nativeElement as HTMLElement;

    expect(element.querySelector('a.skip-link')?.getAttribute('href')).toBe('#main-content');
    expect(element.querySelector('main#main-content')).toBeTruthy();
    expect(element.querySelector('app-header')).toBeTruthy();
    expect(element.querySelector('app-footer')).toBeTruthy();
  });

  it('shows login and register links to anonymous visitors', async () => {
    const fixture = TestBed.createComponent(App);
    await fixture.whenStable();
    const links = Array.from((fixture.nativeElement as HTMLElement).querySelectorAll('header a')).map((a) =>
      a.textContent?.trim(),
    );

    expect(links).toContain('Se connecter');
    expect(links).toContain('Créer un compte');
  });
});
