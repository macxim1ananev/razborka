"use client";

import { useSearchParams } from "next/navigation";
import Link from "next/link";
import { useEffect, useMemo, useState } from "react";
import { fetchCategoriesTree, fetchListings } from "@/lib/api";
import { ListingCard } from "@/components/ListingCard";
import { Pagination } from "@/components/Pagination";
import type { CategoryTreeNode, ListingPreview } from "@/lib/types";

export default function HomePage() {
  const sp = useSearchParams();
  const page = Math.max(0, Number(sp.get("page") || "0") || 0);
  const [data, setData] = useState<{
    content: ListingPreview[];
    totalPages: number;
  }>({ content: [], totalPages: 0 });
  const [err, setErr] = useState("");
  const [loading, setLoading] = useState(true);
  const [rootCategories, setRootCategories] = useState<CategoryTreeNode[]>([]);

  const categoryChips = useMemo(
    () =>
      rootCategories.flatMap((root) =>
        (root.children ?? []).map((c) => ({
          id: c.id,
          name: c.name,
        }))
      ),
    [rootCategories]
  );

  useEffect(() => {
    let ok = true;
    void fetchCategoriesTree()
      .then((tree) => {
        if (ok) setRootCategories(tree);
      })
      .catch(() => {
        if (ok) setRootCategories([]);
      });
    return () => {
      ok = false;
    };
  }, []);

  useEffect(() => {
    let ok = true;
    setLoading(true);
    void fetchListings(page, 20)
      .then((r) => {
        if (ok) {
          setData({ content: r.content, totalPages: r.totalPages });
          setErr("");
        }
      })
      .catch((e) => {
        if (ok) setErr(e instanceof Error ? e.message : "Ошибка загрузки");
      })
      .finally(() => {
        if (ok) setLoading(false);
      });
    return () => {
      ok = false;
    };
  }, [page]);

  return (
    <>
      <section className="hero">
        <div>
          <h1>Рекомендации для вас</h1>
          <p className="muted">Запчасти с авторазбора по всей России: двигатели, КПП, кузов и комплектующие.</p>
        </div>
      </section>

      <section className="category-strip" aria-label="Категории запчастей">
        <Link href="/search" className="category-chip category-chip--muted">
          Все запчасти
        </Link>
        {categoryChips.map((item) => (
          <Link key={item.id} href={`/search/category/${item.id}`} className="category-chip">
            {item.name}
          </Link>
        ))}
      </section>

      {loading && <p className="muted">Загрузка…</p>}
      {err && <p className="form-error">{err}</p>}
      {!loading && !err && (
        <>
          <h2 className="section-title">Объявления</h2>
          <div className="grid-cards">
            {data.content.map((item) => (
              <ListingCard key={item.id} item={item} />
            ))}
          </div>
          {data.content.length === 0 && <p className="muted">Пока нет объявлений.</p>}
          <Pagination basePath="/" page={page} totalPages={data.totalPages} />
        </>
      )}
    </>
  );
}
