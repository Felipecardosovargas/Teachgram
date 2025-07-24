import { cookies } from 'next/headers';
import { NextResponse } from 'next/server';

export async function POST() {
  const cookieStore = await cookies();

  cookieStore.delete('token');
  cookieStore.delete('userId');
  cookieStore.delete('userName');

  return NextResponse.json({ message: 'Logout realizado com sucesso' });
}
