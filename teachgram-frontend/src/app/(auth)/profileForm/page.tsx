"use client";

import React, { useState } from "react";
import { useForm } from "react-hook-form";
import Input from "@/components/ui/Input";
import Button from "@/components/ui/Button";
import Spinner from "@/components/ui/Spinner";
import toast from "react-hot-toast";
import Image from "next/image";
import { useRouter } from "next/navigation";

type ProfileFormData = {
  photoUrl: string;
};

export default function ProfilePage() {
  const [isLoading, setIsLoading] = useState(false);
  const { register, handleSubmit, formState: { errors } } = useForm<ProfileFormData>();
  const router = useRouter();

  const onSubmit = (data: ProfileFormData) => {
    setIsLoading(true);

    // Simula envio
    setTimeout(() => {
      setIsLoading(false);
      toast.success("Link salvo com sucesso!");
      // Opcional: redirecionar para outra página
    }, 1500);
  };

  return (
    <main className="max-w-md mx-auto p-4 relative">
      {/* Botão voltar */}
      <button
        onClick={() => router.push("/signup")}
        className="absolute top-4 left-4"
        aria-label="Voltar para cadastro"
      >
        <Image
          src="/back.svg"
          alt="Voltar"
          width={24}
          height={24}
        />
      </button>

      <form onSubmit={handleSubmit(onSubmit)} className="space-y-6 pt-16">
        <h1 className="font-bold text-xl sm:text-2xl py-[30px]">
          Insira o link da sua foto de perfil
        </h1>

        <Input
          label="Link"
          id="photoUrl"
          type="url"
          placeholder="Insira seu link"
          {...register("photoUrl", { required: "O link é obrigatório." })}
          error={errors.photoUrl?.message}
          disabled={isLoading}
          className={errors.photoUrl ? "border-[var(--color-primary)]" : ""}
        />

        <Button
          type="submit"
          className="w-full"
          style={{ backgroundColor: "var(--color-primary)" }}
          disabled={isLoading}
        >
          {isLoading ? <Spinner size="sm" /> : "Salvar"}
        </Button>
      </form>
    </main>
  );
}
