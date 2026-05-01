export const API_URL =
  (typeof process !== "undefined" && process.env.NEXT_PUBLIC_API_URL) ||
  "http://localhost:8080";

export const TELEGRAM_BOT_NAME =
  process.env.NEXT_PUBLIC_TELEGRAM_BOT_NAME || "";

export const DEV_LOGIN_ENABLED =
  process.env.NEXT_PUBLIC_DEV_LOGIN === "true" ||
  process.env.NEXT_PUBLIC_DEV_LOGIN === "1";

const autoDevLoginRaw = process.env.NEXT_PUBLIC_AUTO_DEV_LOGIN;
export const AUTO_DEV_LOGIN_ENABLED =
  autoDevLoginRaw == null ? true : autoDevLoginRaw === "true" || autoDevLoginRaw === "1";
