import './globals.css';
import { Inter } from 'next/font/google';
import { Suspense } from 'react';
import Loading from './loading';
import QueryProvider from '@/context/QueryProvider';
import { AuthProvider } from '@/context/AuthContext';
import ToastProvider from '@/components/messages/ToastProvider';

const inter = Inter({ subsets: ['latin'] });

export const metadata = {
  title: 'TeachGram - Sua Rede Social de Conhecimento',
  description: 'Uma rede social para compartilhar e aprender.',
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="pt-BR">
      <body className={inter.className}>
        <Suspense fallback={<Loading />}>
          <AuthProvider>
            <QueryProvider>
              <ToastProvider>
                {children}
              </ToastProvider> 
            </QueryProvider>
          </AuthProvider>
        </Suspense>
      </body>
    </html>
  );
}
