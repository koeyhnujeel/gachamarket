export async function apiGet<T>(path: string): Promise<T> {
  const apiBaseUrl = process.env.API_BASE_URL ?? "http://localhost:8080";
  const response = await fetch(`${apiBaseUrl}${path}`, {
    credentials: "include",
    headers: {
      "Content-Type": "application/json",
    },
    cache: "no-store",
  });

  if (!response.ok) {
    throw new Error(`GET ${path} failed`);
  }

  return response.json() as Promise<T>;
}
