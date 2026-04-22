import { useEffect, useState } from 'react'
import * as api from '../api/client'
import type { ProductResponse } from '../api/types'
import { ProductCard } from '../components/ProductCard'
import { useCart } from '../context/CartContext'

export function HomePage() {
  const [products, setProducts] = useState<ProductResponse[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [search, setSearch] = useState('')
  const { add } = useCart()

  useEffect(() => {
    api
      .fetchProducts()
      .then(setProducts)
      .catch((e: Error) => setError(e.message))
      .finally(() => setLoading(false))
  }, [])

  const filteredProducts = products.filter((p) =>
    p.name.toLowerCase().includes(search.toLowerCase())
  )

  if (loading) return <p>Cargando productos…</p>
  if (error) return <p className="alert">{error}</p>

  return (
    <>
      <h2>Catálogo</h2>
      <p style={{ color: '#555' }}>Explora los productos. Para comprar deberás iniciar sesión al finalizar el pedido.</p>

      {/* Buscador */}
      <div style={{ marginBottom: '1rem' }}>
        <input
          type="text"
          placeholder="Buscar producto por nombre..."
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          style={{
            padding: '0.5rem 1rem',
            width: '100%',
            maxWidth: '400px',
            borderRadius: '8px',
            border: '1px solid #ccc',
            fontSize: '1rem'
          }}
        />
      </div>

      <div className="grid">
        {filteredProducts.map((p) => (
          <ProductCard key={p.id} product={p} onAdd={(prod) => add(prod, 1)} />
        ))}
      </div>
      {filteredProducts.length === 0 && <p>No se encontraron productos.</p>}
    </>
  )
}