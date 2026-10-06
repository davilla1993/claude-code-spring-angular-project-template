export type Role = 'ADMIN' | 'USER';

export const ROLES: readonly Role[] = ['ADMIN', 'USER'];

export const ROLE_LABELS: Record<Role, string> = {
  ADMIN: 'Administrateur',
  USER: 'Utilisateur',
};

/** Codes de permission renvoyés par le backend (iam/domain/Permission). */
export type Permission = 'user:create' | 'user:update' | 'user:view' | 'audit:log:view';

export interface User {
  publicId: string;
  email: string;
  firstName: string;
  lastName: string;
  role: Role;
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
  role: Role;
}

export interface UpdateUserPayload {
  firstName: string;
  lastName: string;
  role: Role;
}
