import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { Permission } from '../../shared/models/user.model';
import { SessionStore } from '../services/session-store';

/**
 * Restreint une route à une permission. Confort d'interface uniquement :
 * le backend refuse de toute façon l'accès aux données (403).
 *
 * Usage : `canActivate: [authGuard, permissionGuard('user:view')]`
 */
export function permissionGuard(permission: Permission): CanActivateFn {
  return () => (inject(SessionStore).hasPermission(permission) ? true : inject(Router).createUrlTree(['/app']));
}
