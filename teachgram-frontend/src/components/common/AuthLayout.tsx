"use client";

import React from "react";
import Link from "next/link";
import { usePathname } from "next/navigation";
import Logo from "@/components/ui/Logo";
import Image from "next/image";

interface AuthLayoutProps {
  children: React.ReactNode;
}

const AuthLayout: React.FC<AuthLayoutProps> = ({ children }) => {
  const pathname = usePathname();

  let title = "";
  if (pathname === "/login") {
    title = "Faça seu login";
  } else if (pathname === "/signup") {
    title = "Crie sua conta";
  }

  return (
    <div className="flex min-h-screen">
    <div className="auth-content w-[calc(100%-720px)] flex flex-col justify-center items-center p-8">
      <div className="max-w-md w-full space-y-8 rounded-lg">
        <div className="text-center max-[390px]:flex max-[390px]:justify-center max-[390px]:items-center">
          <Link href="/">
            <Logo className="scale-[0.7] md:scale-100" />
          </Link>
        </div>
        <p className="text-xl font-semibold text-left">{title}</p>
        {children}
      </div>
    </div>

    <div className="w-[720px] h-screen relative hide-image-on-small">
    <Image
        src="/auth-img.png"
        alt="Imagem de autenticação"
        fill
        sizes="(max-width: 768px) 100vw, 50vw"
        style={{
          objectFit: "contain",
          objectPosition: "right",
        }}
        priority
      />
    </div>
  </div>
  );
};

export default AuthLayout;
