'use client';

import React from 'react';
import { useRouter } from 'next/navigation';
import Spinner from '@/components/ui/Spinner';

interface MainLayoutProps {
  children: React.ReactNode;
}

const MainLayout: React.FC<MainLayoutProps> = ({ children }) => {
  const router = useRouter();
  
  if (status === 'loading') {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-100">
        <Spinner size="lg" />
        <p className="ml-4 text-lg text-gray-700">Verificando sessão...</p>
      </div>
    );
  }

  if (status === 'unauthenticated') {
    router.push('/login'); 
    return null; 
  }

  return (
    <div className="flex flex-col min-h-screen bg-gray-100">
      <main className="flex-grow container mx-auto px-4 py-8">
        {children}
      </main>
    </div>
  );
};

export default MainLayout;