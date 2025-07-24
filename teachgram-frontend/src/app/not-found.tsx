"use client";

import Image from "next/image";
import Link from "next/link";
import { useState } from "react";

export default function NotFound() {
  const [hover, setHover] = useState(false);

  return (
    <main className="flex flex-col items-center justify-center min-h-screen p-4 text-center">
      <div className="relative w-full h-64"> 
      <h1 className="font-bold text-7xl text-emerald-500">404</h1>
        <Image
          src="/404.png"
          alt="Imagem de ilustração erro 404 página não encontrada"
          fill
          style={{ objectFit: "contain" }}
        />
      </div>
      <p className="text-xl mb-6">Página não encontrada...</p>
      <Link
        href="/"
        className="px-6 py-3 rounded text-white transition"
        style={{
          backgroundColor: hover ? "color-mix(in srgb, var(--color-primary) 80%, transparent)" : "var(--color-primary)",
          transition: "background-color 0.3s ease",
        }}
        onMouseEnter={() => setHover(true)}
        onMouseLeave={() => setHover(false)}
      >
        Voltar para a Home
      </Link>
    </main>
  );
}
