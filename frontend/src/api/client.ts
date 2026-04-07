import type { AuthResponse, ProductResponse } from './types'

const JSON_HDR = { 'Content-Type': 'application/json' }

export async function fetchProducts(): Promise<ProductResponse[]> {
  const r = await fetch('/api/products')
  if (!r.ok) throw new Error('No se pudieron cargar los productos')
  return r.json()
}

async function readError(r: Response): Promise<string> {
  const text = await r.text()
  try {
    const j = JSON.parse(text) as { message?: string }
    return j.message ?? (text || r.statusText)
  } catch {
    return text || r.statusText
  }
}

export async function register(email: string, password: string, fullName: string): Promise<AuthResponse> {
  const r = await fetch('/api/auth/register', {
    method: 'POST',
    headers: JSON_HDR,
    body: JSON.stringify({ email, password, fullName }),
  })
  if (!r.ok) throw new Error(await readError(r))
  return r.json()
}

export async function login(email: string, password: string): Promise<AuthResponse> {
  const r = await fetch('/api/auth/login', {
    method: 'POST',
    headers: JSON_HDR,
    body: JSON.stringify({ email, password }),
  })
  if (!r.ok) throw new Error(await readError(r))
  return r.json()
}

export async function createOrder(
  token: string,
  items: { productId: string; quantity: number }[],
): Promise<{ orderId: string; totalAmount: number; status: string }> {
  const r = await fetch('/api/orders', {
    method: 'POST',
    headers: { ...JSON_HDR, Authorization: `Bearer ${token}` },
    body: JSON.stringify({ items }),
  })
  if (!r.ok) throw new Error(await readError(r))
  return r.json() as Promise<{ orderId: string; totalAmount: number; status: string }>
}
