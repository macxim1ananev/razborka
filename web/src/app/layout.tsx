import type { Metadata } from "next";
import { Nav } from "@/components/Nav";
import { AuthProvider } from "@/components/AuthProvider";
import "./globals.css";

export const dynamic = "force-dynamic";

export const metadata: Metadata = {
  title: "Разборка — запчасти с авторазбора",
  description: "Маркетплейс автозапчастей с авторазбора",
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="ru">
      <body>
        <AuthProvider>
          <Nav />
          <main className="page">{children}</main>
        </AuthProvider>
      </body>
    </html>
  );
}
