"use client";

import Link from "next/link";
import { useParams, useRouter, useSearchParams } from "next/navigation";
import { useEffect, useMemo, useState } from "react";
import { ListingCard } from "@/components/ListingCard";
import { buildSearchQuery, fetchCategoriesTree, fetchVehicleCatalog, searchListings } from "@/lib/api";
import type { CategoryTreeNode, FacetBucket, SearchHit, SearchResponse, VehicleMakeOption } from "@/lib/types";

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

export default function CategorySearchPage() {
  const routeParams = useParams<{ categoryId: string }>();
  const categoryId = Number(routeParams.categoryId);
  const sp = useSearchParams();
  const router = useRouter();

  const params = useMemo(() => {
    const o: Record<string, string> = { categoryId: String(categoryId) };
    sp.forEach((v, k) => {
      if (k !== "categoryId") o[k] = v;
    });
    return o;
  }, [sp, categoryId]);

  const page = Math.max(0, Number(params.page || "0") || 0);
  const size = Math.min(50, Math.max(1, Number(params.size || "20") || 20));

  const [data, setData] = useState<SearchResponse | undefined>(undefined);
  const [catalogRoot, setCatalogRoot] = useState<CategoryTreeNode | undefined>(undefined);
  const [vehicleCatalog, setVehicleCatalog] = useState<VehicleMakeOption[]>([]);
  const [brandValue, setBrandValue] = useState(params.brand ?? "");
  const [modelValue, setModelValue] = useState(params.model ?? "");
  const [generationValue, setGenerationValue] = useState(params.generation ?? "");
  const [brandListOpen, setBrandListOpen] = useState(false);
  const [modelListOpen, setModelListOpen] = useState(false);
  const [err, setErr] = useState("");
  const [loading, setLoading] = useState(true);

  const queryString = useMemo(() => {
    const q: Record<string, string | undefined> = { ...params, page: String(page), size: String(size) };
    return buildSearchQuery(q);
  }, [params, page, size]);

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
      .catch(() => {
        if (ok) setErr("Поиск временно недоступен. Проверьте backend/api и обновите страницу.");
      })
      .finally(() => {
        if (ok) setLoading(false);
      });
    return () => {
      ok = false;
    };
  }, [queryString]);

  useEffect(() => {
    void fetchCategoriesTree()
      .then((tree) => {
        const root = tree.find((r) => r.slug === "parts") ?? tree[0];
        setCatalogRoot(root);
      })
      .catch(() => {
        setCatalogRoot(undefined);
      });
  }, []);

  useEffect(() => {
    void fetchVehicleCatalog()
      .then((catalog) => {
        setVehicleCatalog(catalog);
      })
      .catch(() => {
        setVehicleCatalog([]);
      });
  }, []);

  useEffect(() => {
    setBrandValue(params.brand ?? "");
    setModelValue(params.model ?? "");
    setGenerationValue(params.generation ?? "");
  }, [params.brand, params.model, params.generation]);

  const categoryName = useMemo(() => {
    const current = catalogRoot?.children?.find((c) => c.id === categoryId);
    return current?.name ?? "Запчасти";
  }, [catalogRoot, categoryId]);

  const queryDefaults = useMemo(
    () => ({
      q: params.q ?? "",
      vin: params.vin ?? "",
      city: params.city ?? "",
      minPrice: params.minPrice ?? "",
      maxPrice: params.maxPrice ?? "",
    }),
    [params]
  );

  const resolvedMake = useMemo(() => {
    const b = brandValue.trim();
    if (!b) return undefined;
    return vehicleCatalog.find((item) => item.name === b);
  }, [brandValue, vehicleCatalog]);

  const availableModels = useMemo(() => resolvedMake?.models ?? [], [resolvedMake]);

  const resolvedModel = useMemo(() => {
    const m = modelValue.trim();
    if (!m) return undefined;
    return availableModels.find((item) => item.name === m);
  }, [availableModels, modelValue]);

  const availableGenerations = useMemo(() => resolvedModel?.generations ?? [], [resolvedModel]);

  const brandSuggestions = useMemo(() => {
    const q = brandValue.trim().toLowerCase();
    if (!q) return vehicleCatalog.slice(0, 60);
    return vehicleCatalog.filter((item) => item.name.toLowerCase().includes(q)).slice(0, 80);
  }, [brandValue, vehicleCatalog]);

  const modelSuggestions = useMemo(() => {
    const q = modelValue.trim().toLowerCase();
    if (!q) return availableModels.slice(0, 60);
    return availableModels.filter((item) => item.name.toLowerCase().includes(q)).slice(0, 80);
  }, [modelValue, availableModels]);

  function facetHref(facetKey: string, value: string) {
    const next = { ...params, [facetKey]: value, page: "0" };
    return `/search/category/${categoryId}${buildSearchQuery(next)}`;
  }

  function clearFacet(facetKey: string) {
    const next = { ...params };
    delete next[facetKey];
    next.page = "0";
    return `/search/category/${categoryId}${buildSearchQuery(next)}`;
  }

  function submitFilters(e: React.FormEvent<HTMLFormElement>) {
    e.preventDefault();
    const fd = new FormData(e.currentTarget);
    const q = readField(fd, "q");
    const vin = readField(fd, "vin");
    const city = readField(fd, "city");
    const minPrice = readField(fd, "minPrice");
    const maxPrice = readField(fd, "maxPrice");

    const next = {
      q,
      vin,
      brand: brandValue,
      model: modelValue,
      generation: generationValue,
      city,
      minPrice,
      maxPrice,
      size: params.size,
      catalogBlock: params.catalogBlock,
      partCondition: params.partCondition,
      originalReplica: params.originalReplica,
    };
    router.push(`/search/category/${categoryId}${buildSearchQuery(next)}`);
  }

  return (
    <>
      <nav className="breadcrumbs" aria-label="Хлебные крошки">
        <Link href="/">Главная</Link>
        <span className="breadcrumbs__sep">/</span>
        <Link href="/search">Автозапчасти</Link>
        <span className="breadcrumbs__sep">/</span>
        <span className="breadcrumbs__current">{categoryName}</span>
      </nav>
      <h1>{categoryName}</h1>
      <p className="muted" style={{ marginTop: "-0.15rem" }}>
        Категория запчастей с фильтрами для точного подбора
      </p>
      <div className="category-results-layout">
        <aside className="category-filters">
          <h2>Фильтры</h2>
          <form className="category-filters__form" onSubmit={submitFilters}>
            <label className="field">
              Поиск
              <input name="q" defaultValue={queryDefaults.q} placeholder="Название запчасти" />
            </label>
            <label className="field">
              VIN
              <input name="vin" defaultValue={queryDefaults.vin} placeholder="17 символов" />
            </label>
            <label className="field combo-field">
              Марка
              <div className="combo">
                <input
                  name="brand"
                  autoComplete="off"
                  value={brandValue}
                  placeholder="Начните вводить марку"
                  onFocus={() => setBrandListOpen(true)}
                  onBlur={() => {
                    window.setTimeout(() => setBrandListOpen(false), 180);
                  }}
                  onChange={(e) => {
                    setBrandValue(e.target.value);
                    setModelValue("");
                    setGenerationValue("");
                    setBrandListOpen(true);
                  }}
                />
                {brandListOpen && brandSuggestions.length > 0 && (
                  <ul className="combo__list" role="listbox">
                    {brandSuggestions.map((make) => (
                      <li key={make.id}>
                        <button
                          type="button"
                          className="combo__option"
                          onMouseDown={(e) => e.preventDefault()}
                          onClick={() => {
                            setBrandValue(make.name);
                            setModelValue("");
                            setGenerationValue("");
                            setBrandListOpen(false);
                          }}
                        >
                          {make.name}
                        </button>
                      </li>
                    ))}
                  </ul>
                )}
              </div>
            </label>
            <label className="field combo-field">
              Модель
              <div className="combo">
                <input
                  name="model"
                  autoComplete="off"
                  value={modelValue}
                  disabled={!resolvedMake}
                  placeholder={resolvedMake ? "Начните вводить модель" : "Сначала выберите марку из списка"}
                  onFocus={() => {
                    if (resolvedMake) setModelListOpen(true);
                  }}
                  onBlur={() => {
                    window.setTimeout(() => setModelListOpen(false), 180);
                  }}
                  onChange={(e) => {
                    setModelValue(e.target.value);
                    setGenerationValue("");
                    setModelListOpen(true);
                  }}
                />
                {modelListOpen && resolvedMake && modelSuggestions.length > 0 && (
                  <ul className="combo__list" role="listbox">
                    {modelSuggestions.map((model) => (
                      <li key={model.id}>
                        <button
                          type="button"
                          className="combo__option"
                          onMouseDown={(e) => e.preventDefault()}
                          onClick={() => {
                            setModelValue(model.name);
                            setGenerationValue("");
                            setModelListOpen(false);
                          }}
                        >
                          {model.name}
                        </button>
                      </li>
                    ))}
                  </ul>
                )}
              </div>
            </label>
            <label className="field">
              Поколение
              <select
                name="generation"
                value={generationValue}
                disabled={!resolvedModel}
                onChange={(e) => setGenerationValue(e.target.value)}
              >
                <option value="">Выберите поколение</option>
                {availableGenerations.map((generation) => (
                  <option key={generation.id} value={generation.name}>
                    {generation.name}
                  </option>
                ))}
              </select>
            </label>
            <label className="field">
              Город
              <input name="city" defaultValue={queryDefaults.city} placeholder="Москва" />
            </label>
            <div className="category-filters__row">
              <label className="field">
                Цена от
                <input name="minPrice" defaultValue={queryDefaults.minPrice} inputMode="numeric" />
              </label>
              <label className="field">
                Цена до
                <input name="maxPrice" defaultValue={queryDefaults.maxPrice} inputMode="numeric" />
              </label>
            </div>
            <div className="category-filters__actions">
              <button type="submit" className="btn btn--primary">
                Применить
              </button>
              <Link href={`/search/category/${categoryId}`} className="btn btn--ghost">
                Сбросить
              </Link>
            </div>
          </form>
          {data?.facets && (
            <div className="category-filters__facets">
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
                    href={`/search/category/${categoryId}${buildSearchQuery({
                      ...params,
                      page: String(data.page - 1),
                      size: String(size),
                    })}`}
                    className="btn btn--ghost"
                  >
                    Назад
                  </Link>
                )}
                {data.page * size + data.content.length < data.totalElements && (
                  <Link
                    href={`/search/category/${categoryId}${buildSearchQuery({
                      ...params,
                      page: String(data.page + 1),
                      size: String(size),
                    })}`}
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

function readField(fd: FormData, key: string): string {
  const raw = fd.get(key);
  if (typeof raw !== "string") {
    return "";
  }
  return raw.trim();
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
