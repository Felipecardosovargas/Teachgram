import { cookies } from "next/headers";

export async function GET() {
  const token = cookies().get("authToken")?.value;

  if (!token) return new Response("Unauthorized", { status: 401 });

  const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/users/me`, {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });

  const data = await response.json();
  return Response.json(data);
}
