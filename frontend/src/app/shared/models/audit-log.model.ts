export type AuditStatus = 'SUCCESS' | 'FAILED';

export interface AuditLog {
  id: string;
  userEmail: string | null;
  /** Profil actif de l'auteur au moment de l'action. */
  activeProfile: string | null;
  action: string;
  entityType: string;
  entityId: string | null;
  details: string | null;
  ipAddress: string | null;
  status: AuditStatus;
  errorMessage: string | null;
  actionDate: string;
}

/** Libellés des actions journalisées par le backend. Une action sans libellé est affichée par son code. */
export const AUDIT_ACTION_LABELS: Record<string, string> = {
  LOGIN: 'Connexion',
  REGISTER: 'Inscription',
  PROFILE_SELECT: 'Choix du profil',
  PASSWORD_CHANGE: 'Changement du mot de passe',
  PASSWORD_RESET: 'Réinitialisation du mot de passe',
  PASSWORD_SETUP: 'Définition du mot de passe',
  USER_CREATE: "Création d'un utilisateur",
  USER_UPDATE: "Modification d'un utilisateur",
  USER_ACTIVATE: "Activation d'un utilisateur",
  USER_DEACTIVATE: "Désactivation d'un utilisateur",
  USER_PASSWORD_RESET: "Réinitialisation du mot de passe d'un utilisateur",
};

/** Libellés des types d'entité journalisés. Un type sans libellé est affiché par son code. */
export const AUDIT_ENTITY_LABELS: Record<string, string> = {
  USER: 'Utilisateur',
};

export interface AuditLogFilters {
  userEmail?: string;
  entityType?: string;
  status?: AuditStatus;
  /** Date-heure ISO locale (ex : 2026-01-31T08:00). */
  from?: string;
  to?: string;
}
