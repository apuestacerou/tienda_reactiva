import type { AuthResponse, CategoryResponse, ProductResponse } from './types'

const JSON_HDR = { 'Content-Type': 'application/json' }

export async function fetchProducts(): Promise<ProductResponse[]> {
  const r = await fetch('/api/products')
  if (!r.ok) throw new Error('No se pudieron cargar los productos')
  return r.json()
}

export async function fetchCategories(): Promise<CategoryResponse[]> {
  const r = await fetch('/api/categories')
  if (!r.ok) throw new Error('No se pudieron cargar las categorías')
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

export async function adminCreateProduct(
  token: string,
  fields: { name: string; description: string; price: number; stock: number; categoryId?: string | null },
  image?: File | null,
): Promise<ProductResponse> {
  const fd = new FormData()
  fd.append('name', fields.name)
  fd.append('description', fields.description ?? '')
  fd.append('price', String(fields.price))
  fd.append('stock', String(fields.stock))
  fd.append('categoryId', fields.categoryId ?? '')
  if (image) fd.append('image', image)
  const r = await fetch('/api/products', {
    method: 'POST',
    headers: { Authorization: `Bearer ${token}` },
    body: fd,
  })
  if (!r.ok) throw new Error(await readError(r))
  return r.json()
}

export async function adminUpdateProduct(
  token: string,
  productId: string,
  fields: { name: string; description: string; price: number; stock: number; categoryId?: string | null },
  image?: File | null,
): Promise<ProductResponse> {
  const fd = new FormData()
  fd.append('name', fields.name)
  fd.append('description', fields.description ?? '')
  fd.append('price', String(fields.price))
  fd.append('stock', String(fields.stock))
  fd.append('categoryId', fields.categoryId ?? '')
  if (image) fd.append('image', image)
  const r = await fetch(`/api/products/${productId}`, {
    method: 'PUT',
    headers: { Authorization: `Bearer ${token}` },
    body: fd,
  })
  if (!r.ok) throw new Error(await readError(r))
  return r.json()
}

export async function adminDeleteProduct(token: string, productId: string): Promise<void> {
  const r = await fetch(`/api/products/${productId}`, {
    method: 'DELETE',
    headers: { Authorization: `Bearer ${token}` },
  })
  if (!r.ok) throw new Error(await readError(r))
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

// ---------------------- FUNCIONES DE APIS PARA LOS USUSARIOS
//para mostrar usuarios
export async function fetchUsers() {
  const r=await fetch('api/users')
  if (!r.ok)throw new Error('No se pudieron cargar los usuarios')
    return r.json()
}
// para buscar usuarios por el id
export async function fetchUsersById(id:string) {
  const r=await fetch(`/api/users/${id}`)
  if (!r.ok)throw new Error('Usuario no encontrado')
    return r.json()
}
//para buscar usuarios por el nombre
export async function serchUsersByName(name:string) {
  const r=await fetch(`/api/users/search?name=${encodeURIComponent(name)}`)
  if (!r.ok)throw new Error('Error buscando usuarios')
    return r.json()
}
//para actualizar al usuario por medio del id
export async function updateUser(id:string, data:any) {
  const r=await fetch(`/api/users/${id}`,{
    method:'PUT',
    headers:{'Content-Type':'application/json'},
    body: JSON.stringify(data),
  })
  if (!r.ok)throw new Error('Error actualizando usuario')
    return r.json()
}
