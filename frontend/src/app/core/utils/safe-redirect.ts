/**
 * Retourne returnUrl s'il s'agit d'un chemin interne, sinon le chemin par défaut.
 * Empêche les redirections ouvertes (ex : returnUrl=//evil.com ou https://evil.com).
 */
export function safeRedirect(returnUrl: string | null | undefined, fallback = '/app'): string {
  if (!returnUrl || !returnUrl.startsWith('/') || returnUrl.startsWith('//') || returnUrl.startsWith('/\\')) {
    return fallback;
  }
  return returnUrl;
}
