export interface ProductResponse {
  id: string
  name: string
  description: string
  price: number
  stock: number
  imageUrl: string | null
  categoryId?: string | null
  categoryName?: string | null
}

export interface CategoryResponse {
  id: string
  name: string
  slug: string
}

export interface AuthResponse {
  token: string
  userId: string
  email: string
  /** CLIENTE | ADMINISTRADOR (según Neon / registro) */
  role: string
}

export interface CartLine {
  productId: string
  name: string
  price: number
  quantity: number
  imageUrl: string | null
}
