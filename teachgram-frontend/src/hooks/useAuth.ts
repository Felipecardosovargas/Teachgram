import { useState } from 'react';

export function useAuth() {
  const [user, setUser] = useState(null);
  const [token, setToken] = useState<string | null>(null);

  async function login(username: string, password: string) {
    const res = await fetch('/api/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username, password }),
    });

    if (!res.ok) {
      throw new Error('Login falhou');
    }

    const data = await res.json();

    setToken(data.token);
    setUser(data.user);
    localStorage.setItem('token', data.token); // ou use cookies
  }

  function logout() {
    localStorage.removeItem('token');
    setToken(null);
    setUser(null);
  }

  return { user, token, login, logout, isAuthenticated: !!user };
}
