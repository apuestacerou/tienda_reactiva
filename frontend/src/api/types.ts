export interface ProductResponse {
  id: string
  name: string
  description: string
  price: number
  stock: number
  imageUrl: string | null
}

export interface AuthResponse {
  token: string
  userId: string
  email: string
}

export interface CartLine {
  productId: string
  name: string
  price: number
  quantity: number
  imageUrl: string | null
}
