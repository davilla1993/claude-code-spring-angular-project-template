/** Enveloppe commune à toutes les réponses de l'API (voir docs/API_CONTRACT.md — Error model). */
export interface ApiResponse<T> {
  success: boolean;
  message?: string;
  data?: T;
  /** Présent uniquement pour les erreurs de validation (400). */
  errors?: string[];
}

/** Page de résultats (voir docs/API_CONTRACT.md — Pagination). */
export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}

export interface PageRequest {
  page: number;
  size: number;
  sort?: string;
}
