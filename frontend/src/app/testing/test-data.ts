import { User } from '../shared/models/user.model';

/** Utilisateur de test ; surcharger les champs nécessaires. */
export function aUser(overrides: Partial<User> = {}): User {
  return {
    publicId: 'user-1',
    email: 'jane@example.com',
    firstName: 'Jane',
    lastName: 'Doe',
    role: 'USER',
    permissions: [],
    active: true,
    emailVerified: true,
    firstLogin: false,
    createdAt: '2026-01-01T10:00:00',
    ...overrides,
  };
}
