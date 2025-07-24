'use client';
import { useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { useAuth } from '@/context/AuthContext';
import Spinner from '@/components/ui/Spinner';

export default function ProtectedRoute({ children }: { children: React.ReactNode }) {
  const { user, accessToken, loading, isAuthenticated } = useAuth();
  const router = useRouter();

  useEffect(() => {
    if (!loading && !isAuthenticated) {
      router.replace('/login');
    }
  }, [loading, isAuthenticated, router]);

  if (loading) {
    return (
      <div className="flex items-center justify-center h-screen">
        <Spinner /> 
      </div>
    );
  }

  if (!user || !accessToken) {
    return null;
  }

  return <>{children}</>;
}

