"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { createListing, fetchCategoriesTree, updateListing } from "@/lib/api";
import { flattenCategories, type FlatCategory } from "@/lib/categories";
import type { ListingDetail } from "@/lib/types";

type Compat = {
  brand: string;
  model: string;
  generation: string;
  yearFrom: string;
  yearTo: string;
  engineVolume: string;
};

const emptyCompat = (): Compat => ({
  brand: "",
  model: "",
  generation: "",
  yearFrom: "",
  yearTo: "",
  engineVolume: "",
});

export function ListingForm({ mode, listingId, initial }: { mode: "create" | "edit"; listingId?: number; initial?: ListingDetail }) {
  const router = useRouter();
  const [cats, setCats] = useState<FlatCategory[]>([]);
  const [categoryId, setCategoryId] = useState(String(initial?.categoryId ?? ""));
  const [title, setTitle] = useState(initial?.title ?? "");
  const [description, setDescription] = useState(initial?.description ?? "");
  const [price, setPrice] = useState(initial != null ? String(initial.price) : "");
  const [condition, setCondition] = useState(initial?.condition ?? "");
  const [originalReplica, setOriginalReplica] = useState(initial?.originalReplica ?? "");
  const [vin, setVin] = useState(initial?.vin ?? "");
  const [catalogBlock, setCatalogBlock] = useState(initial?.catalogBlock ?? "");
  const [mileageKm, setMileageKm] = useState(initial?.mileageKm != null ? String(initial.mileageKm) : "");
  const [vehicleYear, setVehicleYear] = useState(initial?.vehicleYear != null ? String(initial.vehicleYear) : "");
  const [compat, setCompat] = useState<Compat[]>(() =>
    initial?.compatibility?.length
      ? initial.compatibility.map((c) => ({
          brand: c.brand ?? "",
          model: c.model ?? "",
          generation: c.generation ?? "",
          yearFrom: c.yearFrom != null ? String(c.yearFrom) : "",
          yearTo: c.yearTo != null ? String(c.yearTo) : "",
          engineVolume: c.engineVolume != null ? String(c.engineVolume) : "",
        }))
      : [emptyCompat()]
  );
  const [err, setErr] = useState("");
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    void fetchCategoriesTree().then((tree) => setCats(flattenCategories(tree)));
  }, []);

  function buildPayload() {
    const cid = Number(categoryId);
    if (!cid) throw new Error("Выберите категорию");
    const p = Number(price.replace(",", "."));
    if (!p || p < 0.01) throw new Error("Укажите цену");
    const compatibility = compat
      .map((c) => ({
        brand: c.brand || undefined,
        model: c.model || undefined,
        generation: c.generation || undefined,
        yearFrom: c.yearFrom ? Number(c.yearFrom) : undefined,
        yearTo: c.yearTo ? Number(c.yearTo) : undefined,
        engineVolume: c.engineVolume ? Number(c.engineVolume.replace(",", ".")) : undefined,
      }))
      .filter((c) => c.brand || c.model || c.generation || c.yearFrom || c.yearTo || c.engineVolume);
    return {
      categoryId: cid,
      title: title.trim(),
      description: description.trim() || undefined,
      price: p,
      condition: condition || undefined,
      originalReplica: originalReplica || undefined,
      vin: vin.trim().toUpperCase() || undefined,
      catalogBlock: catalogBlock || undefined,
      mileageKm: mileageKm ? Number(mileageKm) : undefined,
      vehicleYear: vehicleYear ? Number(vehicleYear) : undefined,
      compatibility: compatibility.length ? compatibility : undefined,
    };
  }

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault();
    setErr("");
    setSaving(true);
    try {
      const payload = buildPayload();
      if (mode === "create") {
        const created = await createListing(payload);
        router.push(`/listings/${created.id}`);
        router.refresh();
      } else if (listingId != null) {
        await updateListing(listingId, payload);
        router.push(`/listings/${listingId}`);
        router.refresh();
      }
    } catch (e) {
      setErr(e instanceof Error ? e.message : "Ошибка сохранения");
    } finally {
      setSaving(false);
    }
  }

  return (
    <form className="form-grid" onSubmit={(e) => void onSubmit(e)} style={{ maxWidth: 560 }}>
      <label className="field">
        Категория
        <select value={categoryId} onChange={(e) => setCategoryId(e.target.value)} required>
          <option value="">— выберите —</option>
          {cats.map((c) => (
            <option key={c.id} value={c.id}>
              {c.label}
            </option>
          ))}
        </select>
      </label>
      <label className="field">
        Заголовок
        <input value={title} onChange={(e) => setTitle(e.target.value)} required maxLength={255} />
      </label>
      <label className="field">
        Описание
        <textarea value={description} onChange={(e) => setDescription(e.target.value)} maxLength={10000} />
      </label>
      <label className="field">
        Цена (₽)
        <input value={price} onChange={(e) => setPrice(e.target.value)} required inputMode="decimal" />
      </label>
      <label className="field">
        Состояние
        <select value={condition} onChange={(e) => setCondition(e.target.value)}>
          <option value="">—</option>
          <option value="used">Б/у</option>
          <option value="new">Новая</option>
          <option value="refurbished">Восстановленная</option>
        </select>
      </label>
      <label className="field">
        Оригинал / аналог
        <select value={originalReplica} onChange={(e) => setOriginalReplica(e.target.value)}>
          <option value="">—</option>
          <option value="original">Оригинал</option>
          <option value="replica">Аналог</option>
          <option value="oem">OEM</option>
        </select>
      </label>
      <label className="field">
        VIN автомобиля
        <input
          value={vin}
          onChange={(e) => setVin(e.target.value.toUpperCase())}
          placeholder="17 символов, без I/O/Q"
          maxLength={17}
        />
      </label>
      <label className="field">
        Агрегатный блок
        <select value={catalogBlock} onChange={(e) => setCatalogBlock(e.target.value)}>
          <option value="">—</option>
          <option value="engine">Двигатель</option>
          <option value="transmission">Коробка передач</option>
          <option value="suspension">Ходовая</option>
          <option value="body">Кузов</option>
          <option value="steering">Рулевое управление</option>
          <option value="brakes">Тормозная система</option>
          <option value="electrical">Электрика</option>
          <option value="cooling">Охлаждение</option>
          <option value="fuel_exhaust">Топливная и выхлоп</option>
          <option value="interior">Салон</option>
          <option value="other">Другое</option>
        </select>
      </label>
      <label className="field">
        Год авто (для анонса)
        <input value={vehicleYear} onChange={(e) => setVehicleYear(e.target.value)} inputMode="numeric" />
      </label>
      <label className="field">
        Пробег (км)
        <input value={mileageKm} onChange={(e) => setMileageKm(e.target.value)} inputMode="numeric" />
      </label>
      <div>
        <h2 className="muted" style={{ fontSize: "1rem", marginBottom: "0.5rem" }}>
          Совместимость (необязательно)
        </h2>
        {compat.map((row, i) => (
          <div
            key={i}
            style={{
              display: "grid",
              gap: "0.5rem",
              marginBottom: "0.75rem",
              padding: "0.75rem",
              border: "1px solid var(--border)",
              borderRadius: 8,
            }}
          >
            <input placeholder="Марка" value={row.brand} onChange={(e) => setRow(i, { brand: e.target.value })} />
            <input placeholder="Модель" value={row.model} onChange={(e) => setRow(i, { model: e.target.value })} />
            <input
              placeholder="Поколение (например, E90, GJ, II рест.)"
              value={row.generation}
              onChange={(e) => setRow(i, { generation: e.target.value })}
            />
            <div style={{ display: "flex", gap: "0.5rem" }}>
              <input placeholder="Год от" value={row.yearFrom} onChange={(e) => setRow(i, { yearFrom: e.target.value })} />
              <input placeholder="Год до" value={row.yearTo} onChange={(e) => setRow(i, { yearTo: e.target.value })} />
            </div>
            <input
              placeholder="Объём двигателя"
              value={row.engineVolume}
              onChange={(e) => setRow(i, { engineVolume: e.target.value })}
            />
            {compat.length > 1 && (
              <button type="button" className="btn btn--ghost" onClick={() => removeRow(i)}>
                Удалить строку
              </button>
            )}
          </div>
        ))}
        <button type="button" className="btn btn--ghost" onClick={() => setCompat((c) => [...c, emptyCompat()])}>
          + строка совместимости
        </button>
      </div>
      {err && <p className="form-error">{err}</p>}
      <button type="submit" className="btn btn--primary" disabled={saving}>
        {saving ? "Сохранение…" : mode === "create" ? "Создать" : "Сохранить"}
      </button>
    </form>
  );

  function setRow(i: number, patch: Partial<Compat>) {
    setCompat((rows) => rows.map((r, j) => (j === i ? { ...r, ...patch } : r)));
  }

  function removeRow(i: number) {
    setCompat((rows) => rows.filter((_, j) => j !== i));
  }
}
