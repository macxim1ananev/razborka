import type { TelegramWidgetUser } from "./types";

/** Собирает строку initData для POST /auth/telegram из объекта виджета (без пустых полей, кроме обязательных). */
export function telegramUserToInitData(user: TelegramWidgetUser): string {
  const params = new URLSearchParams();
  params.set("id", String(user.id));
  if (user.first_name) params.set("first_name", user.first_name);
  if (user.last_name) params.set("last_name", user.last_name);
  if (user.username) params.set("username", user.username);
  if (user.photo_url) params.set("photo_url", user.photo_url);
  params.set("auth_date", String(user.auth_date));
  params.set("hash", user.hash);
  return params.toString();
}
