"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { useAuth } from "./AuthProvider";
import { activateMyCar, fetchMyCars } from "@/lib/api";
import type { UserCar } from "@/lib/types";

export function Nav() {
  const { authenticated, loading, logout, user } = useAuth();
  const [q, setQ] = useState("");
  const [cars, setCars] = useState<UserCar[]>([]);
  const [switchingCar, setSwitchingCar] = useState(false);
  const router = useRouter();

  useEffect(() => {
    if (!authenticated) {
      setCars([]);
      return;
    }
    let ok = true;
    void fetchMyCars()
      .then((list) => {
        if (ok) setCars(list);
      })
      .catch(() => {
        if (ok) setCars([]);
      });
    return () => {
      ok = false;
    };
  }, [authenticated]);

  function submitSearch(e: React.FormEvent<HTMLFormElement>) {
    e.preventDefault();
    const query = q.trim();
    router.push(query ? `/search?q=${encodeURIComponent(query)}` : "/search");
  }

  async function onActiveCarChange(e: React.ChangeEvent<HTMLSelectElement>) {
    const id = Number(e.target.value);
    if (!id) return;
    setSwitchingCar(true);
    try {
      await activateMyCar(id);
      const refreshed = await fetchMyCars();
      setCars(refreshed);
      router.refresh();
    } finally {
      setSwitchingCar(false);
    }
  }

  const activeCarId = cars.find((c) => c.active)?.id ?? 0;

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
            {authenticated && cars.length > 0 && (
              <label className="car-switcher">
                <span>Активный авто</span>
                <select value={activeCarId} onChange={(e) => void onActiveCarChange(e)} disabled={switchingCar}>
                  {cars.map((car) => (
                    <option key={car.id} value={car.id}>
                      {car.displayName}
                    </option>
                  ))}
                </select>
              </label>
            )}
            {authenticated && <Link href="/me/listings">Мои объявления</Link>}
            {authenticated && <Link href="/me/cars">Мои авто</Link>}
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
