"use client";

import Link from "next/link";

type Props = {
  basePath: string;
  page: number;
  totalPages: number;
  query?: Record<string, string>;
};

export function Pagination({ basePath, page, totalPages, query = {} }: Props) {
  if (totalPages <= 1) return <></>;

  const q = new URLSearchParams(query);
  const href = (p: number) => {
    q.set("page", String(p));
    const s = q.toString();
    return `${basePath}${s ? `?${s}` : ""}`;
  };

  return (
    <nav className="pagination" aria-label="Страницы">
      {page > 0 && (
        <Link href={href(page - 1)} className="btn btn--ghost">
          Назад
        </Link>
      )}
      <span className="pagination__info">
        {page + 1} / {totalPages}
      </span>
      {page < totalPages - 1 && (
        <Link href={href(page + 1)} className="btn btn--ghost">
          Вперёд
        </Link>
      )}
    </nav>
  );
}
