"use client";

import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useRef,
  useState,
  type ReactNode,
} from "react";
import { clearTokens, getAccessToken } from "@/lib/auth-storage";
import { authDevToken, fetchMe } from "@/lib/api";
import type { UserProfile } from "@/lib/types";
import { AUTO_DEV_LOGIN_ENABLED } from "@/lib/config";

type AuthCtx = {
  user: UserProfile | undefined;
  loading: boolean;
  authenticated: boolean;
  reload: () => Promise<void>;
  logout: () => void;
};

const Ctx = createContext<AuthCtx | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<UserProfile | undefined>(undefined);
  const [loading, setLoading] = useState(true);
  const triedAutoDevLoginRef = useRef(false);

  const reload = useCallback(async () => {
    if (!getAccessToken()) {
      const isLocalhost =
        typeof window !== "undefined" &&
        (window.location.hostname === "localhost" || window.location.hostname === "127.0.0.1");
      if (!triedAutoDevLoginRef.current && isLocalhost && AUTO_DEV_LOGIN_ENABLED) {
        triedAutoDevLoginRef.current = true;
        try {
          await authDevToken();
        } catch {
          // Если dev-login недоступен (например, не dev-профиль), просто продолжаем как гость.
        }
      }
      if (!getAccessToken()) {
        setUser(undefined);
        setLoading(false);
        return;
      }
    }
    try {
      const me = await fetchMe();
      setUser(me);
    } catch {
      setUser(undefined);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void reload();
  }, [reload]);

  const logout = useCallback(() => {
    clearTokens();
    setUser(undefined);
  }, []);

  const value = useMemo<AuthCtx>(
    () => ({
      user,
      loading,
      authenticated: Boolean(user),
      reload,
      logout,
    }),
    [user, loading, reload, logout]
  );

  return <Ctx.Provider value={value}>{children}</Ctx.Provider>;
}

export function useAuth(): AuthCtx {
  const v = useContext(Ctx);
  if (!v) {
    throw new Error("useAuth вне AuthProvider");
  }
  return v;
}
