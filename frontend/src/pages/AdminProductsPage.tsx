import { FormEvent, useCallback, useEffect, useState } from 'react'
import { Link, Navigate } from 'react-router-dom'
import {
  adminCreateProduct,
  adminDeleteProduct,
  adminUpdateProduct,
  fetchCategories,
  fetchProducts,
} from '../api/client'
import type { CategoryResponse, ProductResponse } from '../api/types'
import { useAuth } from '../context/AuthContext'

function money(n: number) {
  return n.toLocaleString('es-CO', { style: 'currency', currency: 'COP', maximumFractionDigits: 0 })
}

export function AdminProductsPage() {
  const { token, isAdmin } = useAuth()
  const [products, setProducts] = useState<ProductResponse[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [formError, setFormError] = useState<string | null>(null)
  const [saving, setSaving] = useState(false)

  const [categories, setCategories] = useState<CategoryResponse[]>([])
  const [editingId, setEditingId] = useState<string | null>(null)
  const [name, setName] = useState('')
  const [description, setDescription] = useState('')
  const [price, setPrice] = useState('')
  const [stock, setStock] = useState('')
  const [categoryId, setCategoryId] = useState('')
  const [image, setImage] = useState<File | null>(null)

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

  if (!token) {
    return <Navigate to="/login?next=/admin" replace />
  }
  if (!isAdmin) {
    return (
      <>
        <h2>Administración</h2>
        <p className="alert">Tu cuenta no tiene rol de administrador. Si debería, actualiza el rol en Neon y vuelve a iniciar sesión.</p>
        <p>
          <Link to="/">Volver al catálogo</Link>
        </p>
      </>
    )
  }

  function startEdit(p: ProductResponse) {
    setEditingId(p.id)
    setName(p.name)
    setDescription(p.description ?? '')
    setPrice(String(p.price))
    setStock(String(p.stock))
    setCategoryId(p.categoryId ?? '')
    setImage(null)
    setFormError(null)
  }

  function clearForm() {
    setEditingId(null)
    setName('')
    setDescription('')
    setPrice('')
    setStock('')
    setCategoryId('')
    setImage(null)
    setFormError(null)
  }

  async function onSubmit(e: FormEvent) {
    e.preventDefault()
    if (!token) return
    setFormError(null)
    const priceNum = Number(price)
    const stockNum = parseInt(stock, 10)
    if (!name.trim()) {
      setFormError('El nombre es obligatorio')
      return
    }
    if (Number.isNaN(priceNum) || priceNum < 0) {
      setFormError('Precio inválido')
      return
    }
    if (Number.isNaN(stockNum) || stockNum < 0) {
      setFormError('Stock inválido')
      return
    }
    setSaving(true)
    try {
      const fields = {
        name: name.trim(),
        description: description.trim(),
        price: priceNum,
        stock: stockNum,
        categoryId: categoryId || null,
      }
      if (editingId) {
        await adminUpdateProduct(token, editingId, fields, image)
      } else {
        await adminCreateProduct(token, fields, image)
      }
      clearForm()
      await load()
    } catch (err) {
      setFormError(err instanceof Error ? err.message : 'Error al guardar')
    } finally {
      setSaving(false)
    }
  }

  async function onDelete(id: string, nombre: string) {
    if (!token) return
    if (!window.confirm(`¿Eliminar el producto «${nombre}»?`)) return
    setError(null)
    try {
      await adminDeleteProduct(token, id)
      if (editingId === id) clearForm()
      await load()
    } catch (e) {
      setError(e instanceof Error ? e.message : 'No se pudo eliminar')
    }
  }

  return (
    <>
      <h2>Administración — productos</h2>
      <p style={{ marginTop: 0, opacity: 0.85 }}>
        Alta, edición y baja del catálogo. El catálogo público sigue en{' '}
        <Link to="/">inicio</Link>.
      </p>

      {error && <p className="alert">{error}</p>}

      <section className="card admin-form-card" style={{ marginBottom: '1.5rem' }}>
        <h3 style={{ marginTop: 0 }}>{editingId ? 'Editar producto' : 'Nuevo producto'}</h3>
        {formError && <p className="alert">{formError}</p>}
        <form className="form admin-form" onSubmit={onSubmit}>
          <label>
            Nombre
            <input value={name} onChange={(e) => setName(e.target.value)} required maxLength={255} />
          </label>
          <label>
            Descripción
            <textarea
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              rows={3}
              maxLength={4000}
              style={{ font: 'inherit', padding: '0.5rem', borderRadius: 4, border: '1px solid #ccc' }}
            />
          </label>
          <div className="admin-form-row">
            <label>
              Precio
              <input type="number" min={0} step="0.01" value={price} onChange={(e) => setPrice(e.target.value)} required />
            </label>
            <label>
              Stock
              <input type="number" min={0} step={1} value={stock} onChange={(e) => setStock(e.target.value)} required />
            </label>
          </div>
          <label>
            Categoría
            <select value={categoryId} onChange={(e) => setCategoryId(e.target.value)} style={{ font: 'inherit', padding: '0.5rem', borderRadius: 4, border: '1px solid #ccc' }}>
              <option value="">Sin categoría</option>
              {categories.map((c) => (
                <option key={c.id} value={c.id}>
                  {c.name}
                </option>
              ))}
            </select>
          </label>
          <label>
            Imagen {editingId ? '(opcional; vacío deja la actual)' : '(opcional)'}
            <input
              type="file"
              accept="image/*"
              onChange={(e) => setImage(e.target.files?.[0] ?? null)}
            />
          </label>
          <div className="admin-form-actions">
            <button type="submit" className="btn" disabled={saving}>
              {saving ? 'Guardando…' : editingId ? 'Actualizar' : 'Crear producto'}
            </button>
            {editingId && (
              <button type="button" className="btn btn-secondary" onClick={clearForm} disabled={saving}>
                Cancelar edición
              </button>
            )}
          </div>
        </form>
      </section>

      <section>
        <h3>Catálogo ({products.length})</h3>
        {loading ? (
          <p>Cargando…</p>
        ) : products.length === 0 ? (
          <p>No hay productos. Crea el primero con el formulario de arriba.</p>
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
                        <div className="admin-desc">{p.description.slice(0, 80)}{p.description.length > 80 ? '…' : ''}</div>
                      ) : null}
                    </td>
                    <td>{p.categoryName ?? '—'}</td>
                    <td>{money(p.price)}</td>
                    <td>{p.stock}</td>
                    <td className="admin-actions">
                      <button type="button" className="btn btn-sm" onClick={() => startEdit(p)}>
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
    </>
  )
}
