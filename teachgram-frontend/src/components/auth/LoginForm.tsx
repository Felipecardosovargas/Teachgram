'use client';

import React, { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';
import Input from '@/components/ui/Input';
import Button from '@/components/ui/Button';
import Spinner from '../ui/Spinner';
import { useRouter } from 'next/navigation';
import { useAuth } from '@/context/AuthContext';
import toast from 'react-hot-toast';
import Link from 'next/link';
import Image from 'next/image';
import axios, { type AxiosError } from 'axios';
import { signIn } from 'next-auth/react';

const loginSchema = z.object({
  email: z.string().min(1, 'Email é obrigatório'),
  password: z.string().min(1, 'Senha é obrigatória'),
});

type LoginFormData = z.infer<typeof loginSchema>;

const LoginForm: React.FC = () => {
  const router = useRouter();
  const { login } = useAuth(); 
  const [isLoading, setIsLoading] = useState(false);

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<LoginFormData>({
    resolver: zodResolver(loginSchema),
  });

  const onSubmit = async (data: LoginFormData) => {
    setIsLoading(true);
    toast.dismiss();

    try {
      const response = await axios.post(`${process.env.NEXT_PUBLIC_API_URL}/api/auth/signin`, {
        email: data.email,
        password: data.password,
      });

      const { token, user } = response.data;

      // Aqui salva no contexto
      login({ accessToken: token, user });

      toast.success('Login realizado com sucesso!');
      router.push('/feed');
    } catch (error) {
      const err = error as AxiosError<{ message?: string }>;
      console.error('Erro no login:', err);
    
      if (err.response?.status === 401) {
        toast.error('Usuário ou senha inválidos');
      } else if (err.response?.data?.message) {
        toast.error(err.response.data.message);
      } else {
        toast.error('Erro ao conectar com o servidor. Tente novamente.');
      }
    }
  };

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
      <Input
        label="E-mail"
        id="email"
        type="email"
        placeholder="Digite seu e-mail"
        {...register('email')}
        autoComplete="email"
        error={errors.email?.message}
        disabled={isLoading}
      />
      <Input
        label="Senha"
        id="password"
        type="password"
        placeholder="Digite sua senha"
        {...register('password')}
        autoComplete="current-password"
        error={errors.password?.message}
        disabled={isLoading}
      />
      <Button
        type="submit"
        className="w-full"
        style={{ backgroundColor: 'var(--color-primary)' }}
        disabled={isLoading}
      >
        {isLoading ? <Spinner size="sm" /> : 'Entrar'}
      </Button>

      <div className="flex justify-between items-center text-sm text-gray-600">
        <label className="inline-flex items-center gap-2 cursor-pointer select-none">
          <input type="checkbox" name="remember" disabled={isLoading} />
          <span>Lembrar senha</span>
        </label>
        <Link href="/forgot-password" className="underline hover:text-gray-800">
          Esqueci minha senha
        </Link>
      </div>

      <div className="text-center text-sm text-gray-600">
        Não possui conta?{' '}
        <Link
          href="/signup"
          className="font-bold text-[var(--color-primary)] underline hover:opacity-80"
        >
          Cadastre-se
        </Link>
      </div>

      <div className="relative">
        <div className="absolute inset-0 flex items-center">
          <div className="w-full border-t border-gray-300" />
        </div>
        <div className="relative flex justify-center text-sm">
          <span className="bg-white px-2 text-gray-500">Entrar com</span>
        </div>
      </div>

      <Button
        type="button"
        variant="secondary"
        className="w-full h-12 flex items-center justify-center gap-2 bg-white shadow"
        onClick={() => {
          setIsLoading(true);
          signIn('google', { callbackUrl: '/feed' });
        }}
        disabled={isLoading}
      >
        {isLoading ? <Spinner size="sm" /> : (
          <div className="flex gap-1.5">
            <Image
              src="/Google.png"
              alt="Google"
              width={20}
              height={20}
            />
            Login com Google
          </div>
        )}
      </Button>
    </form>
  );
};

export default LoginForm;
