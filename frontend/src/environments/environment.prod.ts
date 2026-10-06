export const environment = {
  production: true,
  /**
   * L'API doit être servie sur le même site que le frontend (reverse proxy) :
   * les cookies d'authentification sont SameSite=Strict.
   */
  apiBaseUrl: '/api',
};
