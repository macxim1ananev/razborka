"use client";

import Image from "next/image";
import Link from "next/link";
import { useParams, useRouter } from "next/navigation";
import { useCallback, useEffect, useState } from "react";
import {
  addFavorite,
  deleteListing,
  fetchFavorites,
  fetchListing,
  removeFavorite,
  uploadListingPhoto,
} from "@/lib/api";
import { API_URL } from "@/lib/config";
import { useAuth } from "@/components/AuthProvider";
import { ListingCard } from "@/components/ListingCard";
import type { ListingDetail, ListingPreview } from "@/lib/types";

function fullPhotoUrl(url: string): string {
  if (!url) return "";
  if (url.startsWith("http")) return url;
  return `${API_URL}${url.startsWith("/") ? "" : "/"}${url}`;
}

export default function ListingDetailPage() {
  const params = useParams();
  const id = Number(params.id);
  const router = useRouter();
  const { user, authenticated, loading: authLoading } = useAuth();
  const [listing, setListing] = useState<ListingDetail | undefined>(undefined);
  const [err, setErr] = useState("");
  const [fav, setFav] = useState<boolean | undefined>(undefined);
  const [photoMsg, setPhotoMsg] = useState("");
  const [actionMsg, setActionMsg] = useState("");

  const reload = useCallback(() => {
    if (!id) return;
    void fetchListing(id)
      .then(setListing)
      .catch((e) => setErr(e instanceof Error ? e.message : "Ошибка"));
  }, [id]);

  useEffect(() => {
    reload();
  }, [reload]);

  useEffect(() => {
    if (!authenticated || !id) {
      setFav(undefined);
      return;
    }
    let ok = true;
    void fetchFavorites(0, 100)
      .then((p) => {
        if (ok) setFav(p.content.some((x) => x.id === id));
      })
      .catch(() => {
        if (ok) setFav(undefined);
      });
    return () => {
      ok = false;
    };
  }, [authenticated, id]);

  const isOwner = Boolean(user && listing && user.id === listing.seller.id);

  async function onToggleFavorite() {
    if (!id) return;
    setActionMsg("");
    try {
      if (fav) {
        await removeFavorite(id);
        setFav(false);
      } else {
        await addFavorite(id);
        setFav(true);
      }
    } catch (e) {
      setActionMsg(e instanceof Error ? e.message : "Ошибка");
    }
  }

  async function onPhotos(files: FileList | null) {
    if (!files?.length || !id) return;
    setPhotoMsg("");
    try {
      for (let i = 0; i < files.length; i++) {
        await uploadListingPhoto(id, files[i]!);
      }
      reload();
      setPhotoMsg("Фото загружены");
    } catch (e) {
      setPhotoMsg(e instanceof Error ? e.message : "Ошибка загрузки");
    }
  }

  async function onDelete(sold: boolean) {
    if (!id || !confirm(sold ? "Пометить как продано?" : "Снять с публикации (архив)?")) return;
    setActionMsg("");
    try {
      await deleteListing(id, sold);
      router.push("/me/listings");
      router.refresh();
    } catch (e) {
      setActionMsg(e instanceof Error ? e.message : "Ошибка");
    }
  }

  if (err && !listing) {
    return (
      <>
        <p className="form-error">{err}</p>
        <Link href="/">На главную</Link>
      </>
    );
  }

  if (!listing) {
    return <p className="muted">Загрузка…</p>;
  }

  const tg = listing.seller.telegramUsername;
  const tgLink = tg ? `https://t.me/${tg.replace(/^@/, "")}` : "";

  return (
    <>
      <nav className="muted" style={{ marginBottom: "1rem" }}>
        <Link href="/">Главная</Link>
        {" · "}
        <Link href="/search">Поиск</Link>
      </nav>
      <div className="detail-layout">
        <div className="detail-gallery">
          {listing.photoUrls?.length ? (
            listing.photoUrls.map((u) => (
              <Image
                key={u}
                src={fullPhotoUrl(u)}
                alt=""
                width={800}
                height={600}
                style={{ width: "100%", height: "auto", borderRadius: "var(--radius)", border: "1px solid var(--border)" }}
                unoptimized
              />
            ))
          ) : (
            <div className="card__placeholder" style={{ minHeight: 200 }}>
              Нет фото
            </div>
          )}
          {isOwner && (
            <label className="field" style={{ marginTop: "0.5rem" }}>
              Загрузить фото
              <input type="file" accept="image/*" multiple onChange={(e) => void onPhotos(e.target.files)} />
            </label>
          )}
          {photoMsg && <p className="muted">{photoMsg}</p>}
        </div>
        <div>
          <h1 style={{ marginTop: 0 }}>{listing.title}</h1>
          <div className="card__price" style={{ fontSize: "1.5rem", marginBottom: "0.5rem" }}>
            {new Intl.NumberFormat("ru-RU", { style: "currency", currency: "RUB", maximumFractionDigits: 0 }).format(
              listing.price
            )}
          </div>
          <p className="muted">
            {listing.categoryName} · статус: {listing.status}
            {listing.condition && ` · ${listing.condition}`}
            {listing.originalReplica && ` · ${listing.originalReplica}`}
          </p>
          {listing.description && <p>{listing.description}</p>}
          <div className="detail-actions">
            {!authLoading && authenticated && !isOwner && fav !== undefined && (
              <button type="button" className="btn btn--ghost" onClick={() => void onToggleFavorite()}>
                {fav ? "Убрать из избранного" : "В избранное"}
              </button>
            )}
            {!authLoading && !authenticated && (
              <Link href="/login" className="btn btn--primary">
                Войти, чтобы добавить в избранное
              </Link>
            )}
            {isOwner && (
              <>
                <Link href={`/listings/${id}/edit`} className="btn btn--primary">
                  Редактировать
                </Link>
                <button type="button" className="btn btn--ghost" onClick={() => void onDelete(true)}>
                  Продано
                </button>
                <button type="button" className="btn btn--danger" onClick={() => void onDelete(false)}>
                  В архив
                </button>
              </>
            )}
          </div>
          {actionMsg && <p className="form-error">{actionMsg}</p>}
          <h2>Продавец</h2>
          <p>
            {listing.seller.firstName}
            {listing.seller.city && ` · ${listing.seller.city}`}
          </p>
          {tgLink && (
            <a href={tgLink} target="_blank" rel="noopener noreferrer" className="btn btn--primary" style={{ marginTop: "0.5rem" }}>
              Написать в Telegram @{tg.replace(/^@/, "")}
            </a>
          )}
          {!tgLink && <p className="muted">Username в Telegram не указан</p>}
          {listing.compatibility?.length > 0 && (
            <>
              <h2>Совместимость</h2>
              <table className="compatibility-table">
                <thead>
                  <tr>
                    <th>Марка</th>
                    <th>Модель</th>
                    <th>Годы</th>
                    <th>Объём</th>
                  </tr>
                </thead>
                <tbody>
                  {listing.compatibility.map((c) => (
                    <tr key={c.id}>
                      <td>{c.brand}</td>
                      <td>{c.model}</td>
                      <td>
                        {c.yearFrom ?? "—"} — {c.yearTo ?? "—"}
                      </td>
                      <td>{c.engineVolume ?? "—"}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </>
          )}
          {listing.moreFromSeller?.length > 0 && (
            <>
              <h2>Другие объявления продавца</h2>
              <div className="grid-cards">
                {listing.moreFromSeller.map((m: ListingPreview) => (
                  <ListingCard key={m.id} item={m} />
                ))}
              </div>
            </>
          )}
        </div>
      </div>
    </>
  );
}
