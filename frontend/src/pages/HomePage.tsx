import { useEffect, useState } from 'react'
import * as api from '../api/client'
import type { ProductResponse } from '../api/types'
import { ProductCard } from '../components/ProductCard'
import { useCart } from '../context/CartContext'

export function HomePage() {
  const [products, setProducts] = useState<ProductResponse[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const { add } = useCart()

  useEffect(() => {
    api
      .fetchProducts()
      .then(setProducts)
      .catch((e: Error) => setError(e.message))
      .finally(() => setLoading(false))
  }, [])

  if (loading) return <p>Cargando productos…</p>
  if (error) return <p className="alert">{error}</p>

  return (
    <>
      <h2>Catálogo</h2>
      <p style={{ color: '#555' }}>Explora los productos. Para comprar deberás iniciar sesión al finalizar el pedido.</p>
      <div className="grid">
        {products.map((p) => (
          <ProductCard key={p.id} product={p} onAdd={(prod) => add(prod, 1)} />
        ))}
      </div>
      {products.length === 0 && <p>No hay productos todavía.</p>}
    </>
  )
}
