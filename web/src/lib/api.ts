import { API_URL } from "./config";
import type { AuthResponse, PageDto } from "./types";
import { clearTokens, getAccessToken, getRefreshToken, setTokens } from "./auth-storage";

export class ApiError extends Error {
  constructor(
    message: string,
    public status: number,
    public body?: unknown
  ) {
    super(message);
    this.name = "ApiError";
  }
}

async function refreshAccess(): Promise<boolean> {
  const refresh = getRefreshToken();
  if (!refresh) return false;
  const res = await fetch(`${API_URL}/auth/refresh`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    credentials: "include",
    body: JSON.stringify({ refreshToken: refresh }),
  });
  if (!res.ok) {
    clearTokens();
    return false;
  }
  const data = (await res.json()) as AuthResponse;
  setTokens(data.accessToken, data.refreshToken);
  return true;
}

export async function apiFetch<T>(
  path: string,
  init: RequestInit = {},
  retry = true
): Promise<T> {
  const headers = new Headers(init.headers);
  const token = getAccessToken();
  if (token && !headers.has("Authorization")) {
    headers.set("Authorization", `Bearer ${token}`);
  }
  if (init.body && !(init.body instanceof FormData) && !headers.has("Content-Type")) {
    headers.set("Content-Type", "application/json");
  }

  const res = await fetch(`${API_URL}${path}`, {
    ...init,
    headers,
    credentials: "include",
  });

  if (res.status === 401 && retry && getRefreshToken()) {
    const ok = await refreshAccess();
    if (ok) {
      return apiFetch<T>(path, init, false);
    }
  }

  if (!res.ok) {
    let body: unknown;
    try {
      body = await res.json();
    } catch {
      body = await res.text();
    }
    const msg =
      typeof body === "object" && body !== null && "message" in body
        ? String((body as { message: string }).message)
        : res.statusText;
    throw new ApiError(msg, res.status, body);
  }

  if (res.status === 204) {
    return undefined as T;
  }

  const text = await res.text();
  if (!text) {
    return undefined as T;
  }
  return JSON.parse(text) as T;
}

export async function fetchListings(page: number, size: number) {
  return apiFetch<PageDto<import("./types").ListingPreview>>(
    `/api/listings?page=${page}&size=${size}`
  );
}

export async function fetchListing(id: number) {
  return apiFetch<import("./types").ListingDetail>(`/api/listings/${id}`);
}

export async function fetchCategoriesTree() {
  return apiFetch<import("./types").CategoryTreeNode[]>("/api/categories/tree");
}

export async function fetchVehicleCatalog() {
  return apiFetch<import("./types").VehicleMakeOption[]>("/api/vehicles/catalog");
}

export async function fetchMe() {
  return apiFetch<import("./types").UserProfile>("/api/users/me");
}

export async function updateMe(body: { phone?: string; city?: string; bio?: string }) {
  return apiFetch<import("./types").UserProfile>("/api/users/me", {
    method: "PUT",
    body: JSON.stringify(body),
  });
}

export async function fetchMyListings(page: number, size: number) {
  return apiFetch<PageDto<import("./types").ListingPreview>>(
    `/api/users/me/listings?page=${page}&size=${size}`
  );
}

export async function fetchFavorites(page: number, size: number) {
  return apiFetch<PageDto<import("./types").ListingPreview>>(
    `/api/users/me/favorites?page=${page}&size=${size}`
  );
}

export async function addFavorite(listingId: number) {
  await apiFetch<void>(`/api/favorites/${listingId}`, { method: "POST" });
}

export async function removeFavorite(listingId: number) {
  await apiFetch<void>(`/api/favorites/${listingId}`, { method: "DELETE" });
}

export async function createListing(payload: Record<string, unknown>) {
  return apiFetch<import("./types").ListingDetail>("/api/listings", {
    method: "POST",
    body: JSON.stringify(payload),
  });
}

export async function updateListing(id: number, payload: Record<string, unknown>) {
  return apiFetch<import("./types").ListingDetail>(`/api/listings/${id}`, {
    method: "PUT",
    body: JSON.stringify(payload),
  });
}

export async function deleteListing(id: number, sold: boolean) {
  await apiFetch<void>(`/api/listings/${id}?sold=${sold}`, { method: "DELETE" });
}

export async function uploadListingPhoto(listingId: number, file: File) {
  const fd = new FormData();
  fd.append("file", file);
  const headers = new Headers();
  const token = getAccessToken();
  if (token) headers.set("Authorization", `Bearer ${token}`);
  const res = await fetch(`${API_URL}/api/listings/${listingId}/photos`, {
    method: "POST",
    headers,
    body: fd,
    credentials: "include",
  });
  if (!res.ok) {
    throw new ApiError(await res.text(), res.status);
  }
}

export function buildSearchQuery(params: Record<string, string | undefined>) {
  const q = new URLSearchParams();
  Object.entries(params).forEach(([k, v]) => {
    if (v !== undefined && v !== "") q.set(k, v);
  });
  const s = q.toString();
  return s ? `?${s}` : "";
}

export async function searchListings(query: string) {
  return apiFetch<import("./types").SearchResponse>(`/api/search${query}`);
}

export async function authTelegram(initData: string) {
  const data = await apiFetch<AuthResponse>("/auth/telegram", {
    method: "POST",
    body: JSON.stringify({ initData }),
  });
  setTokens(data.accessToken, data.refreshToken);
  return data;
}

export async function authDevToken(telegramId?: number) {
  const q = telegramId ? `?telegramId=${telegramId}` : "";
  const data = await apiFetch<AuthResponse>(`/auth/dev/token${q}`, {
    method: "POST",
  });
  setTokens(data.accessToken, data.refreshToken);
  return data;
}
