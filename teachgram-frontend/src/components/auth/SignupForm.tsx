'use client';

import React, { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';
import Input from '@/components/ui/Input';
import Button from '@/components/ui/Button';
import Spinner from '@/components/ui/Spinner';
import toast from 'react-hot-toast';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import axios, { AxiosError } from 'axios';

export const signupSchema = z.object({
  name: z.string().min(1, 'Nome é obrigatório.'),
  username: z.string().min(1, 'Nome de usuário é obrigatório.'),
  mail: z.string().email('Email inválido.').min(1, 'Email é obrigatório.'),
  phone: z.string().min(1, 'Telefone é obrigatório.'),
  description: z.string().optional(),
  password: z.string().min(6, 'A senha deve ter no mínimo 6 caracteres.'),
  profileLink: z.string().optional()
});

type SignupFormData = z.infer<typeof signupSchema>;

const SignupForm: React.FC = () => {
  const router = useRouter();
  const [isLoading, setIsLoading] = useState(false);

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<SignupFormData>({
    resolver: zodResolver(signupSchema),
  });

  const onSubmit = async (data: SignupFormData) => {
    setIsLoading(true);
    toast.dismiss();
  
    try {
      await axios.post(`${process.env.NEXT_PUBLIC_API_URL}/api/auth/signup`, {
        name: data.name,
        username: data.username,
        email: data.mail,
        phone: data.phone,
        password: data.password,
        profileLink: data.profileLink ?? '',
        description: data.description || '',
      });
  
      const loginResponse = await axios.post(`${process.env.NEXT_PUBLIC_API_URL}/api/auth/signin`, {
        email: data.mail,
        password: data.password,
      });
  
      const { token } = loginResponse.data;

      await fetch('/api/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ token }),
      });

      toast.success('Cadastro e login realizados com sucesso!');
      router.push('/profileForm');
  
    } catch (error: unknown) {
      console.error('Erro no cadastro/login:', error);
  
      if (error instanceof AxiosError) {
        if (error.response?.status === 409) {
          toast.error('Usuário já existe.');
        } else if (error.response?.status === 401) {
          toast.error('Erro ao logar após cadastro.');
        } else if (error.response?.data?.message) {
          toast.error(error.response.data.message);
        } else {
          toast.error('Erro desconhecido. Tente novamente.');
        }
      } else {
        toast.error('Erro inesperado. Verifique sua conexão.');
      }
  
    } finally {
      setIsLoading(false);
    }
  };  

  return (
    <form 
    onSubmit={handleSubmit(onSubmit)} 
    className="space-y-4 max-w-md mx-auto md:max-w-lg"
    >
      <Input
        label="Nome"
        id="name"
        type="text"
        placeholder="Digite seu nome"
        {...register('name')}
        error={errors.name?.message}
        disabled={isLoading}
        className={errors.name ? 'border-[var(--color-primary)]' : ''}
      />
      <Input
        label="E-mail"
        id="mail"
        type="email"
        placeholder="Digite seu E-mail"
        {...register('mail')}
        error={errors.mail?.message}
        disabled={isLoading}
        className={errors.mail ? 'border-[var(--color-primary)]' : ''}
      />
      <Input
        label="Username"
        id="username"
        type="text"
        placeholder="@ seu_username"
        {...register('username')}
        error={errors.username?.message}
        disabled={isLoading}
        className={errors.username ? 'border-[var(--color-primary)]' : ''}
      />
      <Input
        label="Descrição"
        id="description"
        type="text"
        placeholder="Faça uma descrição"
        {...register('description')}
        error={errors.description?.message}
        disabled={isLoading}
        className={errors.description ? 'border-[var(--color-primary)]' : ''}
      />
      <Input
        label="Celular"
        id="phone"
        type="tel"
        placeholder="Digite seu número de celular"
        {...register('phone')}
        error={errors.phone?.message}
        disabled={isLoading}
        className={errors.phone ? 'border-[var(--color-primary)]' : ''}
      />
      <Input
        label="Senha"
        id="password"
        type="password"
        placeholder="Digite sua senha"
        {...register('password')}
        error={errors.password?.message}
        disabled={isLoading}
        className={errors.password ? 'border-[var(--color-primary)]' : ''}
      />
      {/* Mensagem de erro geral, só aparece se houver qualquer erro */}
      {Object.keys(errors).length > 0 && (
        <p className="text-[var(--color-primary)] font-semibold text-center">
          Campo não preenchido
        </p>
      )}

      <Button
        type="submit"
        className="w-full"
        style={{ backgroundColor: "var(--color-primary)" }}
        disabled={isLoading}
      >
        {isLoading ? <Spinner size="sm" /> : "Próximo"}
      </Button>

      <div className="text-center text-sm text-gray-600">
        Já tem uma conta?{' '}
        <Link
          href="/login"
          className="font-bold text-[var(--color-primary)] hover:text-[color-mix(in srgb, var(--color-primary) 80%, transparent)] underline decoration-[var(--color-primary)] hover:decoration-[color-mix(in srgb, var(--color-primary) 80%, transparent)]"
        >
          Entrar
        </Link>
      </div>
    </form>
  );
};

export default SignupForm;