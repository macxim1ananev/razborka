"use client";

import { useEffect } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { useAuth } from "@/components/AuthProvider";
import { ListingForm } from "@/components/ListingForm";

export default function NewListingPage() {
  const { authenticated, loading } = useAuth();
  const router = useRouter();

  useEffect(() => {
    if (!loading && !authenticated) {
      router.replace("/login");
    }
  }, [authenticated, loading, router]);

  if (loading || !authenticated) {
    return <p className="muted">Проверка входа…</p>;
  }

  return (
    <>
      <h1>Новое объявление</h1>
      <p className="muted">
        После создания откройте карточку и загрузите фото. <Link href="/">На главную</Link>
      </p>
      <ListingForm mode="create" />
    </>
  );
}
