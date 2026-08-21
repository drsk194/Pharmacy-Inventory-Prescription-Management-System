let authorizedUntil = null;
export function setCsAuthorized(expiresAt) { authorizedUntil = expiresAt; }
export function isCsAuthorized() { return Boolean(authorizedUntil && new Date(authorizedUntil) > new Date()); }
export function getCsAuthorizedUntil() { return authorizedUntil; }
export function clearCsAuth() { authorizedUntil = null; }
