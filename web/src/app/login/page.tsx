"use client";

import { useCallback } from "react";
import { useRouter } from "next/navigation";
import { TelegramLogin } from "@/components/TelegramLogin";
import { useAuth } from "@/components/AuthProvider";
import { DEV_LOGIN_ENABLED } from "@/lib/config";
import { authDevToken } from "@/lib/api";
import { useState } from "react";

export default function LoginPage() {
  const { reload } = useAuth();
  const router = useRouter();
  const [devErr, setDevErr] = useState("");
  const [devLoading, setDevLoading] = useState(false);

  const afterAuth = useCallback(() => {
    void reload();
    router.push("/");
    router.refresh();
  }, [reload, router]);

  async function devLogin(telegramId?: number) {
    setDevErr("");
    setDevLoading(true);
    try {
      await authDevToken(telegramId);
      afterAuth();
    } catch (e) {
      setDevErr(e instanceof Error ? e.message : "Ошибка");
    } finally {
      setDevLoading(false);
    }
  }

  return (
    <>
      <h1>Вход</h1>
      <p className="muted">Вход только через Telegram. После авторизации вы сможете создавать объявления и добавлять избранное.</p>
      <section style={{ marginTop: "1.5rem" }}>
        <h2 className="muted" style={{ fontSize: "1rem", marginBottom: "0.75rem" }}>
          Telegram
        </h2>
        <TelegramLogin
          onSuccess={() => {
            void reload();
            router.push("/");
            router.refresh();
          }}
        />
      </section>
      {DEV_LOGIN_ENABLED && (
        <section style={{ marginTop: "2rem", paddingTop: "1.5rem", borderTop: "1px solid var(--border)" }}>
          <h2 className="muted" style={{ fontSize: "1rem", marginBottom: "0.75rem" }}>
            Разработка
          </h2>
          <p className="muted" style={{ marginBottom: "0.75rem" }}>
            Только если API запущено с <code>--spring.profiles.active=dev</code> и есть пользователи из миграции V3.
          </p>
          <div style={{ display: "flex", flexWrap: "wrap", gap: "0.5rem" }}>
            <button type="button" className="btn btn--ghost" disabled={devLoading} onClick={() => void devLogin()}>
              Вход как 999000001
            </button>
            <button
              type="button"
              className="btn btn--ghost"
              disabled={devLoading}
              onClick={() => void devLogin(999000002)}
            >
              Вход как 999000002
            </button>
          </div>
          {devErr && <p className="form-error">{devErr}</p>}
        </section>
      )}
    </>
  );
}
