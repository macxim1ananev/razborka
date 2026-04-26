"use client";

import Link from "next/link";
import { useState } from "react";
import { useRouter } from "next/navigation";
import { useAuth } from "./AuthProvider";

export function Nav() {
  const { authenticated, loading, logout, user } = useAuth();
  const [q, setQ] = useState("");
  const router = useRouter();

  function submitSearch(e: React.FormEvent<HTMLFormElement>) {
    e.preventDefault();
    const query = q.trim();
    router.push(query ? `/search?q=${encodeURIComponent(query)}` : "/search");
  }

  return (
    <header className="site-header">
      <div className="site-header__inner">
        <div className="top-links">
          <div className="top-links__left">
            <Link href="/">Для бизнеса</Link>
            <Link href="/search">Каталог</Link>
            <Link href="/me/favorites">Избранное</Link>
          </div>
          <div className="top-links__right">
            {!loading && !authenticated && <Link href="/login">Вход и регистрация</Link>}
            {authenticated && (
              <button type="button" className="link-btn" onClick={logout}>
                Выход ({user?.firstName || user?.username || user?.id})
              </button>
            )}
          </div>
        </div>
        <div className="header-main">
          <Link href="/" className="logo">
            Разборка
          </Link>
          <form className="search-bar" onSubmit={submitSearch}>
            <input
              value={q}
              onChange={(e) => setQ(e.target.value)}
              placeholder="Поиск по объявлениям"
              aria-label="Поиск по объявлениям"
            />
            <button type="submit" className="search-bar__submit">
              Найти
            </button>
          </form>
          <nav className="nav-links">
            {authenticated && <Link href="/me/listings">Мои объявления</Link>}
            {authenticated && <Link href="/me">Профиль</Link>}
            <Link href="/listings/new" className="post-btn">
              Разместить объявление
            </Link>
          </nav>
        </div>
      </div>
    </header>
  );
}
