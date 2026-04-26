"use client";

import { useEffect, useRef, useState } from "react";
import { authTelegram } from "@/lib/api";
import { TELEGRAM_BOT_NAME } from "@/lib/config";
import { telegramUserToInitData } from "@/lib/telegram";
import type { TelegramWidgetUser } from "@/lib/types";

declare global {
  interface Window {
    onTelegramAuth?: (user: TelegramWidgetUser) => void;
  }
}

export function TelegramLogin({ onSuccess }: { onSuccess?: () => void }) {
  const ref = useRef<HTMLDivElement>(null);
  const onSuccessRef = useRef(onSuccess);
  onSuccessRef.current = onSuccess;
  const [err, setErr] = useState("");

  useEffect(() => {
    if (!TELEGRAM_BOT_NAME) {
      setErr("Задайте NEXT_PUBLIC_TELEGRAM_BOT_NAME в .env.local");
      return;
    }

    window.onTelegramAuth = async (user: TelegramWidgetUser) => {
      setErr("");
      try {
        const initData = telegramUserToInitData(user);
        await authTelegram(initData);
        onSuccessRef.current?.();
      } catch (e) {
        setErr(e instanceof Error ? e.message : "Ошибка входа");
      }
    };

    const container = ref.current;
    if (!container) return;

    const script = document.createElement("script");
    script.src = "https://telegram.org/js/telegram-widget.js?22";
    script.async = true;
    script.setAttribute("data-telegram-login", TELEGRAM_BOT_NAME);
    script.setAttribute("data-size", "large");
    script.setAttribute("data-onauth", "onTelegramAuth(user)");
    script.setAttribute("data-request-access", "write");
    container.appendChild(script);

    return () => {
      delete window.onTelegramAuth;
      script.remove();
    };
  }, []);

  return (
    <div>
      <div ref={ref} className="telegram-widget-wrap" />
      {err && <p className="form-error">{err}</p>}
    </div>
  );
}
