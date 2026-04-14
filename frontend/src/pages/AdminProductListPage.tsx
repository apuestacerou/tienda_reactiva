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
        <h3>Catálogo ({products.length})</h3>
        {loading ? (
          <p>Cargando…</p>
        ) : products.length === 0 ? (
          <p>No hay productos. <Link to="/admin/nuevo">Crea el primero</Link>.</p>
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
                {products.map((p) => (
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
