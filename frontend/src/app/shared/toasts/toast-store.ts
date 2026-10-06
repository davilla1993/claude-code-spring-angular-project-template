import { Injectable, signal } from '@angular/core';

export type ToastKind = 'success' | 'error' | 'info';

export interface Toast {
  id: number;
  kind: ToastKind;
  message: string;
}

const AUTO_DISMISS_MS = 5000;

/** Notifications éphémères. Affichées par ToastContainer (région aria-live). */
@Injectable({ providedIn: 'root' })
export class ToastStore {
  private nextId = 1;
  private readonly items = signal<Toast[]>([]);

  readonly toasts = this.items.asReadonly();

  success(message: string): void {
    this.show('success', message);
  }

  error(message: string): void {
    this.show('error', message);
  }

  info(message: string): void {
    this.show('info', message);
  }

  dismiss(id: number): void {
    this.items.update((toasts) => toasts.filter((toast) => toast.id !== id));
  }

  private show(kind: ToastKind, message: string): void {
    const id = this.nextId++;
    this.items.update((toasts) => [...toasts, { id, kind, message }]);
    setTimeout(() => this.dismiss(id), AUTO_DISMISS_MS);
  }
}
