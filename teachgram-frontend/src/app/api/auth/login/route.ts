import { cookies } from 'next/headers';
import { NextResponse } from 'next/server';

export async function POST(request: Request) {
  const { token, userId, userName, expiresIn } = await request.json();

  if (!token) {
    return NextResponse.json({ message: 'Token não fornecido' }, { status: 400 });
  }

  const cookieStore = await cookies();

  cookieStore.set('token', token, {
    httpOnly: true,
    secure: process.env.NODE_ENV === 'production',
    sameSite: 'lax',
    maxAge: expiresIn ?? 60 * 60 * 24 * 7,
    path: '/',
  });

  cookieStore.set('userId', userId ?? '', {
    path: '/',
    maxAge: expiresIn,
  });

  cookieStore.set('userName', userName ?? '', {
    path: '/',
    maxAge: expiresIn,
  });

  return NextResponse.json({ message: 'Login persistido com sucesso' });
}
