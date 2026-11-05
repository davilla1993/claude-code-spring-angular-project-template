export type Role = 'ADMIN' | 'USER';

export const ROLES: readonly Role[] = ['ADMIN', 'USER'];

export const ROLE_LABELS: Record<Role, string> = {
  ADMIN: 'Administrateur',
  USER: 'Utilisateur',
};

/** Libellés de plusieurs rôles, pour l'affichage. */
export function roleNames(roles: readonly Role[]): string {
  return roles.map((role) => ROLE_LABELS[role]).join(', ');
}

/** Codes de permission renvoyés par le backend (iam/domain/Permission). */
export type Permission = 'user:create' | 'user:update' | 'user:view' | 'audit:log:view';

export interface User {
  publicId: string;
  email: string;
  firstName: string;
  lastName: string;
  roles: Role[];
  /** Profil actif de la session ; null hors session ou tant qu'aucun profil n'est choisi. */
  activeRole: Role | null;
  /** Permissions du seul profil actif. */
  permissions: Permission[];
  active: boolean;
  emailVerified: boolean;
  firstLogin: boolean;
  createdAt: string;
}

export interface CreateUserPayload {
  firstName: string;
  lastName: string;
  email: string;
  temporaryPassword: string;
  roles: Role[];
}

export interface UpdateUserPayload {
  firstName: string;
  lastName: string;
  roles: Role[];
}
