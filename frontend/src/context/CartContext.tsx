import React, { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react'
import type { CartLine, ProductResponse } from '../api/types'

const CART_KEY = 'tienda_cart'

function load(): CartLine[] {
  try {
    const raw = localStorage.getItem(CART_KEY)
    if (!raw) return []
    const parsed = JSON.parse(raw) as CartLine[]
    return Array.isArray(parsed) ? parsed : []
  } catch {
    return []
  }
}

function save(lines: CartLine[]) {
  localStorage.setItem(CART_KEY, JSON.stringify(lines))
}

interface CartContextValue {
  lines: CartLine[]
  add: (p: ProductResponse, qty: number) => void
  remove: (productId: string) => void
  clear: () => void
  total: number
  count: number
}

const CartContext = createContext<CartContextValue | null>(null)

export function CartProvider({ children }: { children: React.ReactNode }) {
  const [lines, setLines] = useState<CartLine[]>(load)

  useEffect(() => {
    save(lines)
  }, [lines])

  const add = useCallback((p: ProductResponse, qty: number) => {
    setLines((prev) => {
      const i = prev.findIndex((l) => l.productId === p.id)
      if (i >= 0) {
        const next = [...prev]
        const q = Math.min(next[i].quantity + qty, p.stock)
        next[i] = { ...next[i], quantity: q }
        return next
      }
      return [...prev, { productId: p.id, name: p.name, price: p.price, quantity: Math.min(qty, p.stock), imageUrl: p.imageUrl }]
    })
  }, [])

  const remove = useCallback((productId: string) => {
    setLines((prev) => prev.filter((l) => l.productId !== productId))
  }, [])

  const clear = useCallback(() => setLines([]), [])

  const total = lines.reduce((s, l) => s + l.price * l.quantity, 0)
  const count = lines.reduce((s, l) => s + l.quantity, 0)

  const value = useMemo(
    () => ({ lines, add, remove, clear, total, count }),
    [lines, add, remove, clear, total, count],
  )

  return <CartContext.Provider value={value}>{children}</CartContext.Provider>
}

export function useCart(): CartContextValue {
  const ctx = useContext(CartContext)
  if (!ctx) throw new Error('useCart dentro de CartProvider')
  return ctx
}
