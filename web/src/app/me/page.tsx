"use client";

import { useRouter } from "next/navigation";
import { useEffect, useState } from "react";
import { fetchMe, updateMe } from "@/lib/api";
import { useAuth } from "@/components/AuthProvider";
import type { UserProfile } from "@/lib/types";

export default function ProfilePage() {
  const { authenticated, loading: authLoading, reload, logout } = useAuth();
  const router = useRouter();
  const [profile, setProfile] = useState<UserProfile | undefined>(undefined);
  const [phone, setPhone] = useState("");
  const [city, setCity] = useState("");
  const [bio, setBio] = useState("");
  const [err, setErr] = useState("");
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    if (!authLoading && !authenticated) {
      router.replace("/login");
    }
  }, [authLoading, authenticated, router]);

  useEffect(() => {
    if (!authenticated) return;
    void fetchMe()
      .then((p) => {
        setProfile(p);
        setPhone(p.phone || "");
        setCity(p.city || "");
        setBio(p.bio || "");
      })
      .catch((e) => setErr(e instanceof Error ? e.message : "Ошибка"));
  }, [authenticated]);

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault();
    setErr("");
    setSaving(true);
    try {
      const updated = await updateMe({ phone: phone || undefined, city: city || undefined, bio: bio || undefined });
      setProfile(updated);
      void reload();
    } catch (e) {
      setErr(e instanceof Error ? e.message : "Ошибка");
    } finally {
      setSaving(false);
    }
  }

  if (authLoading || !authenticated) {
    return <p className="muted">Загрузка…</p>;
  }

  return (
    <>
      <h1>Личный кабинет</h1>
      {profile && (
        <p className="muted">
          Telegram ID: {profile.telegramId}
          {profile.username && ` · @${profile.username}`}
        </p>
      )}
      <form className="form-grid" onSubmit={(e) => void onSubmit(e)}>
        <label className="field">
          Телефон
          <input value={phone} onChange={(e) => setPhone(e.target.value)} maxLength={20} />
        </label>
        <label className="field">
          Город
          <input value={city} onChange={(e) => setCity(e.target.value)} maxLength={255} />
        </label>
        <label className="field">
          О себе
          <textarea value={bio} onChange={(e) => setBio(e.target.value)} maxLength={2000} />
        </label>
        {err && <p className="form-error">{err}</p>}
        <button type="submit" className="btn btn--primary" disabled={saving}>
          {saving ? "Сохранение…" : "Сохранить"}
        </button>
      </form>
      <p style={{ marginTop: "2rem" }}>
        <button type="button" className="btn btn--ghost" onClick={logout}>
          Выйти
        </button>
      </p>
    </>
  );
}
