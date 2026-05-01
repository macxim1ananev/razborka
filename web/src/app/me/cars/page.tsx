"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { activateMyCar, createMyCar, deleteMyCar, fetchMyCars, fetchVehicleCatalog } from "@/lib/api";
import { useAuth } from "@/components/AuthProvider";
import type { UserCar, VehicleMakeOption } from "@/lib/types";

export default function MyCarsPage() {
  const { authenticated, loading: authLoading } = useAuth();
  const router = useRouter();
  const [cars, setCars] = useState<UserCar[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [saving, setSaving] = useState(false);
  const [vehicleCatalog, setVehicleCatalog] = useState<VehicleMakeOption[]>([]);
  const [selectedMakeId, setSelectedMakeId] = useState("");
  const [selectedModelId, setSelectedModelId] = useState("");
  const [selectedGenerationId, setSelectedGenerationId] = useState("");
  const [form, setForm] = useState({
    displayName: "",
    year: "",
    engineVolume: "",
  });

  useEffect(() => {
    if (!authLoading && !authenticated) {
      router.replace("/login");
    }
  }, [authLoading, authenticated, router]);

  useEffect(() => {
    if (!authenticated) return;
    void reloadCars();
    void fetchVehicleCatalog()
      .then((catalog) => setVehicleCatalog(catalog))
      .catch(() => setVehicleCatalog([]));
  }, [authenticated]);

  const selectedMake = vehicleCatalog.find((make) => String(make.id) === selectedMakeId);
  const models = selectedMake?.models ?? [];
  const selectedModel = models.find((model) => String(model.id) === selectedModelId);
  const generations = selectedModel?.generations ?? [];
  const selectedGeneration = generations.find((generation) => String(generation.id) === selectedGenerationId);

  async function reloadCars() {
    setLoading(true);
    setError("");
    try {
      const list = await fetchMyCars();
      setCars(list);
    } catch (e) {
      setError(e instanceof Error ? e.message : "Не удалось загрузить автомобили");
    } finally {
      setLoading(false);
    }
  }

  async function submit(e: React.FormEvent) {
    e.preventDefault();
    setError("");
    setSaving(true);
    try {
      if (!selectedMake || !selectedModel) {
        throw new Error("Выберите марку и модель из каталога");
      }
      const yearValue = form.year.trim() ? Number(form.year) : undefined;
      const displayName =
        form.displayName.trim() ||
        `${selectedMake.name} ${selectedModel.name}${selectedGeneration ? ` ${selectedGeneration.name}` : ""}${
          yearValue ? ` ${yearValue}` : ""
        }`;
      await createMyCar({
        displayName,
        brand: selectedMake.name,
        model: selectedModel.name,
        generation: selectedGeneration?.name,
        year: yearValue,
        engineVolume: form.engineVolume.trim() ? Number(form.engineVolume) : undefined,
      });
      setForm({ displayName: "", year: "", engineVolume: "" });
      setSelectedMakeId("");
      setSelectedModelId("");
      setSelectedGenerationId("");
      await reloadCars();
      router.refresh();
    } catch (e) {
      setError(e instanceof Error ? e.message : "Не удалось добавить автомобиль");
    } finally {
      setSaving(false);
    }
  }

  async function activate(carId: number) {
    await activateMyCar(carId);
    await reloadCars();
    router.refresh();
  }

  async function remove(carId: number) {
    await deleteMyCar(carId);
    await reloadCars();
    router.refresh();
  }

  if (authLoading || !authenticated) return <p className="muted">Загрузка…</p>;

  return (
    <>
      <h1>Мои автомобили</h1>
      <p className="muted">Выберите активный автомобиль — по нему фильтруются объявления и поиск.</p>

      <form className="form-grid" onSubmit={(e) => void submit(e)}>
        <label className="field">
          Название авто (опционально)
          <input
            value={form.displayName}
            onChange={(e) => setForm((prev) => ({ ...prev, displayName: e.target.value }))}
            placeholder="Если пусто, будет сгенерировано автоматически"
          />
        </label>
        <label className="field">
          Марка
          <select
            value={selectedMakeId}
            onChange={(e) => {
              setSelectedMakeId(e.target.value);
              setSelectedModelId("");
              setSelectedGenerationId("");
            }}
            required
          >
            <option value="">Выберите марку</option>
            {vehicleCatalog.map((make) => (
              <option key={make.id} value={make.id}>
                {make.name}
              </option>
            ))}
          </select>
        </label>
        <label className="field">
          Модель
          <select
            value={selectedModelId}
            onChange={(e) => {
              setSelectedModelId(e.target.value);
              setSelectedGenerationId("");
            }}
            disabled={!selectedMake}
            required
          >
            <option value="">{selectedMake ? "Выберите модель" : "Сначала выберите марку"}</option>
            {models.map((model) => (
              <option key={model.id} value={model.id}>
                {model.name}
              </option>
            ))}
          </select>
        </label>
        <label className="field">
          Поколение
          <select
            value={selectedGenerationId}
            onChange={(e) => setSelectedGenerationId(e.target.value)}
            disabled={!selectedModel}
          >
            <option value="">{selectedModel ? "Выберите поколение" : "Сначала выберите модель"}</option>
            {generations.map((generation) => (
              <option key={generation.id} value={generation.id}>
                {generation.name}
              </option>
            ))}
          </select>
        </label>
        <label className="field">
          Год
          <input
            type="number"
            min={1950}
            max={2100}
            value={form.year}
            onChange={(e) => setForm((prev) => ({ ...prev, year: e.target.value }))}
          />
        </label>
        <label className="field">
          Объем двигателя
          <input
            type="number"
            step="0.1"
            min={0.6}
            max={12}
            value={form.engineVolume}
            onChange={(e) => setForm((prev) => ({ ...prev, engineVolume: e.target.value }))}
          />
        </label>
        <button type="submit" className="btn btn--primary" disabled={saving}>
          {saving ? "Добавление…" : "Добавить автомобиль"}
        </button>
      </form>

      {error && <p className="form-error">{error}</p>}

      {loading ? (
        <p className="muted">Загрузка списка автомобилей…</p>
      ) : (
        <div className="list-cards">
          {cars.map((car) => (
            <article key={car.id} className="card">
              <h3 style={{ marginTop: 0 }}>
                {car.displayName} {car.active && <span className="muted">· активный</span>}
              </h3>
              <p className="muted">
                {car.brand} {car.model}
                {car.generation ? ` · ${car.generation}` : ""}
                {car.year ? ` · ${car.year}` : ""}
                {car.engineVolume ? ` · ${car.engineVolume}л` : ""}
              </p>
              <div style={{ display: "flex", gap: "0.5rem" }}>
                {!car.active && (
                  <button type="button" className="btn btn--ghost" onClick={() => void activate(car.id)}>
                    Сделать активным
                  </button>
                )}
                <button type="button" className="btn btn--ghost" onClick={() => void remove(car.id)}>
                  Удалить
                </button>
              </div>
            </article>
          ))}
          {cars.length === 0 && <p className="muted">Пока не добавлено ни одного автомобиля.</p>}
        </div>
      )}
    </>
  );
}
