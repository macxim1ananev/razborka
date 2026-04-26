"use client";

import Link from "next/link";
import { useRouter, useSearchParams } from "next/navigation";
import { useEffect, useState } from "react";
import { fetchFavorites } from "@/lib/api";
import { useAuth } from "@/components/AuthProvider";
import { ListingCard } from "@/components/ListingCard";
import { Pagination } from "@/components/Pagination";
import type { ListingPreview } from "@/lib/types";

export default function FavoritesPage() {
  const sp = useSearchParams();
  const page = Math.max(0, Number(sp.get("page") || "0") || 0);
  const router = useRouter();
  const { authenticated, loading: authLoading } = useAuth();
  const [data, setData] = useState<{ content: ListingPreview[]; totalPages: number }>({
    content: [],
    totalPages: 0,
  });
  const [err, setErr] = useState("");

  useEffect(() => {
    if (!authLoading && !authenticated) {
      router.replace("/login");
    }
  }, [authLoading, authenticated, router]);

  useEffect(() => {
    if (!authenticated) return;
    let ok = true;
    void fetchFavorites(page, 20)
      .then((r) => {
        if (ok) {
          setData({ content: r.content, totalPages: r.totalPages });
          setErr("");
        }
      })
      .catch((e) => {
        if (ok) setErr(e instanceof Error ? e.message : "Ошибка");
      });
    return () => {
      ok = false;
    };
  }, [authenticated, page]);

  if (authLoading || !authenticated) {
    return <p className="muted">Загрузка…</p>;
  }

  return (
    <>
      <h1>Избранное</h1>
      {err && <p className="form-error">{err}</p>}
      <div className="grid-cards">
        {data.content.map((item) => (
          <ListingCard key={item.id} item={item} />
        ))}
      </div>
      {data.content.length === 0 && !err && (
        <p className="muted">
          Пусто. Добавляйте объявления из <Link href="/search">поиска</Link> или <Link href="/">ленты</Link>.
        </p>
      )}
      <Pagination basePath="/me/favorites" page={page} totalPages={data.totalPages} />
    </>
  );
}
