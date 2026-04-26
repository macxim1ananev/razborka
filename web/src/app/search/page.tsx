"use client";

import Link from "next/link";
import { useSearchParams } from "next/navigation";
import { useEffect, useMemo, useState } from "react";
import { buildSearchQuery, fetchCategoriesTree, searchListings } from "@/lib/api";
import { ListingCard } from "@/components/ListingCard";
import type { CategoryTreeNode, FacetBucket, SearchHit, SearchResponse } from "@/lib/types";

type CatalogItem = { id: number; name: string; slug: string };

const FALLBACK_CATALOG_ITEMS: CatalogItem[] = [
  { id: 2, name: "Двигатель", slug: "engine" },
  { id: 3, name: "Трансмиссия и привод", slug: "transmission" },
  { id: 228, name: "Подвеска", slug: "parts_suspension" },
  { id: 234, name: "Рулевое управление", slug: "parts_steering" },
  { id: 227, name: "Тормозная система", slug: "parts_brakes" },
  { id: 229, name: "Электрика", slug: "parts_electrical" },
  { id: 230, name: "Охлаждение", slug: "parts_cooling" },
  { id: 231, name: "Салон", slug: "parts_interior" },
  { id: 233, name: "Топливная система", slug: "parts_fuel" },
  { id: 232, name: "Система выпуска", slug: "parts_exhaust" },
  { id: 4, name: "Кузов", slug: "body" },
];

function hitToPreview(h: SearchHit) {
  return {
    id: h.id,
    title: h.title,
    price: h.price,
    firstPhotoUrl: h.firstPhotoUrl,
    sellerCity: h.sellerCity,
    vehicleYear: h.vehicleYear,
    mileageKm: h.mileageKm,
  };
}

export default function SearchPage() {
  const sp = useSearchParams();
  const params = useMemo(() => {
    const o: Record<string, string> = {};
    sp.forEach((v, k) => {
      o[k] = v;
    });
    return o;
  }, [sp]);

  const page = Math.max(0, Number(params.page || "0") || 0);
  const size = Math.min(50, Math.max(1, Number(params.size || "20") || 20));

  const [data, setData] = useState<SearchResponse | undefined>(undefined);
  const [catalogRoot, setCatalogRoot] = useState<CategoryTreeNode | undefined>(undefined);
  const [catalogLoadFailed, setCatalogLoadFailed] = useState(false);
  const [err, setErr] = useState("");
  const [loading, setLoading] = useState(true);

  const queryString = useMemo(() => {
    const q: Record<string, string | undefined> = { ...params, page: String(page), size: String(size) };
    return buildSearchQuery(q);
  }, [params, page, size]);

  useEffect(() => {
    void fetchCategoriesTree()
      .then((tree) => {
        const root = tree.find((r) => r.slug === "parts") ?? tree[0];
        setCatalogRoot(root);
        setCatalogLoadFailed(false);
      })
      .catch(() => {
        setCatalogRoot(undefined);
        setCatalogLoadFailed(true);
      });
  }, []);

  useEffect(() => {
    let ok = true;
    setLoading(true);
    void searchListings(queryString)
      .then((r) => {
        if (ok) {
          setData(r);
          setErr("");
        }
      })
      .catch((e) => {
        if (ok) setErr("Поиск временно недоступен. Проверьте backend/api и обновите страницу.");
      })
      .finally(() => {
        if (ok) setLoading(false);
      });
    return () => {
      ok = false;
    };
  }, [queryString]);

  const catalogItems = useMemo<CatalogItem[]>(() => {
    const dynamicItems = (catalogRoot?.children ?? []).map((item) => ({
      id: item.id,
      name: item.name,
      slug: item.slug,
    }));
    if (dynamicItems.length > 0) {
      return dynamicItems;
    }
    return FALLBACK_CATALOG_ITEMS;
  }, [catalogRoot]);

  function facetHref(facetKey: string, value: string) {
    const next = { ...params, [facetKey]: value, page: "0" };
    return `/search${buildSearchQuery(next)}`;
  }

  function clearFacet(facetKey: string) {
    const next = { ...params };
    delete next[facetKey];
    next.page = "0";
    return `/search${buildSearchQuery(next)}`;
  }

  return (
    <>
      <h1>Автозапчасти</h1>
      <section className="catalog-showcase" aria-label="Каталог автозапчастей">
        <aside className="catalog-showcase__menu">
          <h3>Фильтры</h3>
          {catalogLoadFailed && (
            <p className="muted" style={{ margin: "0 0 0.5rem" }}>
              Каталог загружен в резервном режиме
            </p>
          )}
          <div className="catalog-showcase__menu-list">
            {catalogItems.map((item) => (
              <Link key={`menu-${item.id}`} href={`/search/category/${item.id}`}>
                {item.name}
              </Link>
            ))}
            <Link href="/search" className="catalog-showcase__all-link">
              Смотреть все →
            </Link>
          </div>
        </aside>
        <div className="catalog-showcase__tiles">
          {catalogItems.map((item) => (
            <Link key={`tile-${item.id}`} href={`/search/category/${item.id}`} className="catalog-tile">
              <span className="catalog-tile__circle">{iconBySlug(item.slug)}</span>
              <span className="catalog-tile__name">{item.name}</span>
            </Link>
          ))}
        </div>
      </section>
      <div className="search-layout">
        <aside>
          <h2>Фасеты</h2>
          {data?.facets && (
            <div>
              {Object.entries(data.facets)
                .filter(([name]) => name !== "priceRanges")
                .map(([name, buckets]) => {
                  const param = mapFacetToParam(name);
                  return (
                    <FacetSection
                      key={name}
                      name={name}
                      buckets={buckets}
                      hrefFor={(v) => facetHref(param, v)}
                      clearHref={param in params ? clearFacet(param) : undefined}
                    />
                  );
                })}
            </div>
          )}
        </aside>
        <div>
          {loading && <p className="muted">Поиск…</p>}
          {err && <p className="form-error">{err}</p>}
          {data && !loading && (
            <>
              <p className="muted">
                Найдено: {data.totalElements} · страница {data.page + 1}
              </p>
              <div className="grid-cards" style={{ marginTop: "1rem" }}>
                {data.content.map((h) => (
                  <ListingCard key={h.id} item={hitToPreview(h)} />
                ))}
              </div>
              {data.content.length === 0 && <p className="muted">Ничего не найдено.</p>}
              <nav className="pagination">
                {data.page > 0 && (
                  <Link
                    href={`/search${buildSearchQuery({ ...params, page: String(data.page - 1), size: String(size) })}`}
                    className="btn btn--ghost"
                  >
                    Назад
                  </Link>
                )}
                {data.page * size + data.content.length < data.totalElements && (
                  <Link
                    href={`/search${buildSearchQuery({ ...params, page: String(data.page + 1), size: String(size) })}`}
                    className="btn btn--ghost"
                  >
                    Вперёд
                  </Link>
                )}
              </nav>
            </>
          )}
        </div>
      </div>
    </>
  );
}

function mapFacetToParam(facetName: string): string {
  const m: Record<string, string> = {
    brands: "brand",
    models: "model",
    generations: "generation",
    catalogBlock: "catalogBlock",
    partCondition: "partCondition",
    originalReplica: "originalReplica",
    sellerCity: "city",
    categoryId: "categoryId",
  };
  return m[facetName] || facetName;
}

function iconBySlug(slug: string): string {
  if (slug.includes("brake")) return "🛑";
  if (slug.includes("steering")) return "🛞";
  if (slug.includes("suspension")) return "🧰";
  if (slug.includes("engine")) return "⚙️";
  if (slug.includes("transmission")) return "🔩";
  if (slug.includes("cooling")) return "❄️";
  if (slug.includes("fuel")) return "⛽";
  if (slug.includes("exhaust")) return "💨";
  if (slug.includes("electrical")) return "🔌";
  if (slug.includes("interior")) return "🪑";
  return "🔧";
}

function FacetSection({
  name,
  buckets,
  hrefFor,
  clearHref,
}: {
  name: string;
  buckets: FacetBucket[];
  hrefFor: (value: string) => string;
  clearHref?: string;
}) {
  if (!buckets?.length) return <></>;
  return (
    <div className="facet-block">
      <h3>{name}</h3>
      {clearHref && (
        <Link href={clearHref} className="muted" style={{ fontSize: "0.8rem" }}>
          сбросить
        </Link>
      )}
      <div className="facet-list">
        {buckets.map((b) => (
          <Link key={`${name}-${b.value}`} href={hrefFor(b.value)} className="facet-pill">
            {b.value} ({b.count})
          </Link>
        ))}
      </div>
    </div>
  );
}
