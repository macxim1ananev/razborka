"use client";

import Image from "next/image";
import Link from "next/link";
import { API_URL } from "@/lib/config";
import type { ListingPreview } from "@/lib/types";

function photoSrc(url: string): string {
  if (!url) return "";
  if (url.startsWith("http")) return url;
  return `${API_URL}${url.startsWith("/") ? "" : "/"}${url}`;
}

export function ListingCard({ item }: { item: ListingPreview }) {
  const src = photoSrc(item.firstPhotoUrl);
  const price = new Intl.NumberFormat("ru-RU", {
    style: "currency",
    currency: "RUB",
    maximumFractionDigits: 0,
  }).format(item.price);

  return (
    <article className="card">
      <Link href={`/listings/${item.id}`} className="card__media">
        {src ? (
          <Image
            src={src}
            alt=""
            width={320}
            height={200}
            className="card__img"
            unoptimized
          />
        ) : (
          <div className="card__placeholder">Нет фото</div>
        )}
      </Link>
      <div className="card__body">
        <Link href={`/listings/${item.id}`} className="card__title">
          {item.title}
        </Link>
        <div className="card__price">{price}</div>
        <div className="card__meta">
          {item.sellerCity && <span>{item.sellerCity}</span>}
          {item.vehicleYear != null && <span> · {item.vehicleYear} г.</span>}
          {item.mileageKm != null && <span> · {item.mileageKm} км</span>}
        </div>
        <div className="card__date">Сегодня</div>
      </div>
    </article>
  );
}
