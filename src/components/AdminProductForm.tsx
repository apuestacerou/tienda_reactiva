import { useCallback, useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { adminDeleteProduct, fetchCategories, fetchProducts } from '../api/client'
import type { CategoryResponse, ProductResponse } from '../api/types'
import { AdminProductForm } from '../components/AdminProductForm'
import { RequireAdmin } from '../components/RequireAdmin'
import { useAuth } from '../context/AuthContext'

function money(n: number) {
  return n.toLocaleString('es-CO', { style: 'currency', currency: 'COP', maximumFractionDigits: 0 })
}

export function AdminProductListPage() {
  const { token } = useAuth()
  const [products, setProducts] = useState<ProductResponse[]>([])
  const [categories, setCategories] = useState<CategoryResponse[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [editingProduct, setEditingProduct] = useState<ProductResponse | null>(null)
  const [search, setSearch] = useState('')
  const [selectedCategory, setSelectedCategory] = useState('')

  const load = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      const list = await fetchProducts()
      setProducts(list)
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Error al cargar productos')
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    load()
  }, [load])

  useEffect(() => {
    fetchCategories()
      .then(setCategories)
      .catch(() => setCategories([]))
  }, [])

  async function onDelete(id: string, nombre: string) {
    if (!token) return
    if (!window.confirm(`¿Eliminar el producto «${nombre}»?`)) return
    setError(null)
    try {
      await adminDeleteProduct(token, id)
      if (editingProduct?.id === id) setEditingProduct(null)
      await load()
    } catch (e) {
      setError(e instanceof Error ? e.message : 'No se pudo eliminar')
    }
  }

  // Filtrar por nombre y categoría
  const filteredProducts = products.filter((p) => {
    const matchName = p.name.toLowerCase().includes(search.toLowerCase())
    const matchCategory = selectedCategory === '' || p.categoryName === selectedCategory
    return matchName && matchCategory
  })

  return (
    <RequireAdmin>
      <Link to="/admin" className="admin-back">
        ← Volver al panel
      </Link>
      <h2>Productos creados</h2>
      <p style={{ marginTop: 0, opacity: 0.85 }}>
        Listado del catálogo. Para dar de alta uno nuevo usa{' '}
        <Link to="/admin/nuevo">Crear producto</Link>.
      </p>

      {error && <p className="alert">{error}</p>}

      {/* Filtros de búsqueda */}
      <div style={{ display: 'flex', gap: '1rem', marginBottom: '1rem', flexWrap: 'wrap' }}>
        <input
          type="text"
          placeholder="Buscar por nombre..."
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          style={{
            padding: '0.5rem 1rem',
            borderRadius: '8px',
            border: '1px solid #ccc',
            fontSize: '1rem',
            minWidth: '200px'
          }}
        />
        <select
          value={selectedCategory}
          onChange={(e) => setSelectedCategory(e.target.value)}
          style={{
            padding: '0.5rem 1rem',
            borderRadius: '8px',
            border: '1px solid #ccc',
            fontSize: '1rem'
          }}
        >
          <option value="">Todas las categorías</option>
          {categories.map((c) => (
            <option key={c.id} value={c.name}>
              {c.name}
            </option>
          ))}
        </select>
      </div>

      {editingProduct && (
        <AdminProductForm
          editingProduct={editingProduct}
          categories={categories}
          onCancelEdit={() => setEditingProduct(null)}
          onSaved={async () => {
            setEditingProduct(null)
            await load()
          }}
        />
      )}

      <section>
        <h3>Catálogo ({filteredProducts.length})</h3>
        {loading ? (
          <p>Cargando…</p>
        ) : filteredProducts.length === 0 ? (
          <p>No hay productos que coincidan con la búsqueda.</p>
        ) : (
          <div className="admin-table-wrap">
            <table className="admin-table">
              <thead>
                <tr>
                  <th>Imagen</th>
                  <th>Nombre</th>
                  <th>Categoría</th>
                  <th>Precio</th>
                  <th>Stock</th>
                  <th />
                </tr>
              </thead>
              <tbody>
                {filteredProducts.map((p) => (
                  <tr key={p.id}>
                    <td>
                      {p.imageUrl ? (
                        <img src={p.imageUrl} alt="" className="admin-thumb" />
                      ) : (
                        <span className="admin-no-img">—</span>
                      )}
                    </td>
                    <td>
                      <strong>{p.name}</strong>
                      {p.description ? (
                        <div className="admin-desc">
                          {p.description.slice(0, 80)}
                          {p.description.length > 80 ? '…' : ''}
                        </div>
                      ) : null}
                    </td>
                    <td>{p.categoryName ?? '—'}</td>
                    <td>{money(p.price)}</td>
                    <td>{p.stock}</td>
                    <td className="admin-actions">
                      <button type="button" className="btn btn-sm" onClick={() => setEditingProduct(p)}>
                        Editar
                      </button>
                      <button type="button" className="btn btn-sm btn-danger" onClick={() => onDelete(p.id, p.name)}>
                        Eliminar
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>
    </RequireAdmin>
  )
}