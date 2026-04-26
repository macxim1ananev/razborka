"use client";

import { useParams, useRouter } from "next/navigation";
import { useEffect, useState } from "react";
import Link from "next/link";
import { fetchListing } from "@/lib/api";
import { useAuth } from "@/components/AuthProvider";
import { ListingForm } from "@/components/ListingForm";
import type { ListingDetail } from "@/lib/types";

export default function EditListingPage() {
  const params = useParams();
  const id = Number(params.id);
  const router = useRouter();
  const { user, loading: authLoading, authenticated } = useAuth();
  const [listing, setListing] = useState<ListingDetail | undefined>(undefined);
  const [err, setErr] = useState("");

  useEffect(() => {
    if (!authLoading && !authenticated) {
      router.replace("/login");
    }
  }, [authLoading, authenticated, router]);

  useEffect(() => {
    if (!id) return;
    let ok = true;
    void fetchListing(id)
      .then((l) => {
        if (ok) setListing(l);
      })
      .catch((e) => {
        if (ok) setErr(e instanceof Error ? e.message : "Ошибка");
      });
    return () => {
      ok = false;
    };
  }, [id]);

  useEffect(() => {
    if (!listing || !user) return;
    if (listing.seller.id !== user.id) {
      setErr("Это не ваше объявление");
    }
  }, [listing, user]);

  if (authLoading || !authenticated) {
    return <p className="muted">Проверка входа…</p>;
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

  if (listing.seller.id !== user?.id) {
    return (
      <>
        <p className="form-error">{err || "Нет доступа"}</p>
        <Link href={`/listings/${id}`}>К объявлению</Link>
      </>
    );
  }

  return (
    <>
      <h1>Редактирование</h1>
      <p className="muted">
        <Link href={`/listings/${id}`}>← к карточке</Link>
      </p>
      <ListingForm mode="edit" listingId={id} initial={listing} />
    </>
  );
}
