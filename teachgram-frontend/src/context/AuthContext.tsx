'use client';

import { createContext, useContext, useEffect, useState } from 'react';
import { parseCookies, setCookie, destroyCookie } from 'nookies';
import { AuthSession, AuthUser } from '@/types/auth';

interface AuthContextType {
  user: AuthUser | null;
  accessToken: string | null;
  isAuthenticated: boolean;
  loading: boolean;
  login: (session: AuthSession) => void;
  logout: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<AuthUser | null>(null);
  const [accessToken, setAccessToken] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function loadUserFromToken() {
      const cookies = parseCookies();
      const token = cookies['accessToken'];

      if (token) {
        setAccessToken(token);
        setLoading(true);

        try {
          const res = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/api/users/me`, {
            headers: {
              Authorization: `Bearer ${token}`,
            },
          });

          if (!res.ok) throw new Error('Token inválido');

          const data = await res.json();

          setUser(data.user ?? data);
        } catch (error) {
          console.error('Erro ao carregar usuário:', error);
          setAccessToken(null);
          setUser(null);
          destroyCookie(null, 'accessToken');
        } finally {
          setLoading(false);
        }
      } else {
        setLoading(false);
      }
    }

    loadUserFromToken();
  }, []);

  const login = (session: AuthSession) => {
    setUser(session.user);
    setAccessToken(session.accessToken);
    setLoading(false);

    setCookie(null, 'accessToken', session.accessToken, {
      maxAge: 60 * 60 * 24 * 7, 
      path: '/',
      secure: true,
      sameSite: 'lax',
    });
    setLoading(false);
  };

  const logout = () => {
    setUser(null);
    setAccessToken(null);
    setLoading(false);
    destroyCookie(null, 'accessToken');
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        accessToken,
        isAuthenticated: !!accessToken && !!user,
        loading,
        login,
        logout,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth(): AuthContextType {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used dentro do AuthProvider');
  }
  return context;
}
