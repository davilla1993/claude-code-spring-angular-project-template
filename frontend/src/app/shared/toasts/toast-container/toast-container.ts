import { Component, inject } from '@angular/core';
import { ToastStore } from '../toast-store';

@Component({
  selector: 'app-toast-container',
  templateUrl: './toast-container.html',
  styleUrl: './toast-container.scss',
})
export class ToastContainer {
  protected readonly store = inject(ToastStore);
}
