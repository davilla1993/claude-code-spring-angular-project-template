import { Injectable, OnDestroy } from '@angular/core';

const CHANNEL_NAME = 'session-sync';

/**
 * Les onglets partagent les cookies, donc l'utilisateur et le profil actif : après une connexion, une déconnexion
 * ou un changement de profil dans un onglet, les autres se rechargent pour ne pas afficher un état périmé.
 */
@Injectable({ providedIn: 'root' })
export class SessionSync implements OnDestroy {
  private readonly channel = typeof BroadcastChannel === 'undefined' ? null : new BroadcastChannel(CHANNEL_NAME);

  constructor() {
    if (this.channel) {
      this.channel.onmessage = () => window.location.assign('/app');
    }
  }

  /** L'onglet émetteur ne reçoit pas son propre message. */
  announceSessionChange(): void {
    this.channel?.postMessage('session-changed');
  }

  ngOnDestroy(): void {
    this.channel?.close();
  }
}
