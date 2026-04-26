const ACCESS = "marketplace_access";
const REFRESH = "marketplace_refresh";

export function getAccessToken(): string {
  if (typeof window === "undefined") return "";
  return sessionStorage.getItem(ACCESS) || "";
}

export function getRefreshToken(): string {
  if (typeof window === "undefined") return "";
  return sessionStorage.getItem(REFRESH) || "";
}

export function setTokens(access: string, refresh: string): void {
  sessionStorage.setItem(ACCESS, access);
  sessionStorage.setItem(REFRESH, refresh);
}

export function clearTokens(): void {
  sessionStorage.removeItem(ACCESS);
  sessionStorage.removeItem(REFRESH);
}
