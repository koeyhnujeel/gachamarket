import { cookies } from "next/headers";

export async function hasSessionCookie() {
  const cookieStore = await cookies();
  return cookieStore.has("JSESSIONID");
}
