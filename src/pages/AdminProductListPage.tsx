import { useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { adminCreateProduct, fetchCategories } from '../api/client'
import type { CategoryResponse } from '../api/types'
import { RequireAdmin } from '../components/RequireAdmin'
import { useAuth } from '../context/AuthContext'

export function AdminProductNewPage() {
  const { token } = useAuth()
  const navigate = useNavigate()
  const [categories, setCategories] = useState<CategoryResponse[]>([])
  const [name, setName] = useState('')
  const [description, setDescription] = useState('')
  const [price, setPrice] = useState('')
  const [stock, setStock] = useState('')
  const [categoryId, setCategoryId] = useState('')
  const [image, setImage] = useState<File | null>(null)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [success, setSuccess] = useState(false)

  useEffect(() => {
    fetchCategories()
      .then(setCategories)
      .catch(() => setCategories([]))
  }, [])

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    if (!token) return
    setError(null)

    const priceNum = Number(price)
    const stockNum = parseInt(stock, 10)

    if (!name.trim()) return setError('El nombre es obligatorio')
    if (isNaN(priceNum) || priceNum < 0) return setError('Precio inválido')
    if (isNaN(stockNum) || stockNum < 0) return setError('Stock inválido')

    setSaving(true)
    try {
      await adminCreateProduct(token, {
        name: name.trim(),
        description: description.trim(),
        price: priceNum,
        stock: stockNum,
        categoryId: categoryId || null,
      }, image)
      setSuccess(true)
      setTimeout(() => navigate('/admin/productos'), 1500)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Error al crear producto')
    } finally {
      setSaving(false)
    }
  }

  return (
    <RequireAdmin>
      <Link to="/admin/productos" className="admin-back">
        ← Volver a productos
      </Link>
      <h2>Crear nuevo producto</h2>

      {error && <p className="alert">{error}</p>}
      {success && <p style={{ color: 'green', fontWeight: 'bold' }}>✅ Producto creado exitosamente. Redirigiendo...</p>}

      <section className="card admin-form-card">
        <form className="form admin-form" onSubmit={handleSubmit}>
          <label>
            Nombre *
            <input
              value={name}
              onChange={(e) => setName(e.target.value)}
              required
              maxLength={255}
              placeholder="Ej: Camiseta deportiva"
            />
          </label>
          <label>
            Descripción
            <textarea
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              rows={3}
              maxLength={4000}
              placeholder="Describe el producto..."
              style={{ font: 'inherit', padding: '0.5rem', borderRadius: 4, border: '1px solid #ccc' }}
            />
          </label>
          <div className="admin-form-row">
            <label>
              Precio *
              <input
                type="number"
                min={0}
                step="0.01"
                value={price}
                onChange={(e) => setPrice(e.target.value)}
                required
                placeholder="0"
              />
            </label>
            <label>
              Stock *
              <input
                type="number"
                min={0}
                step={1}
                value={stock}
                onChange={(e) => setStock(e.target.value)}
                required
                placeholder="0"
              />
            </label>
          </div>
          <label>
            Categoría
            <select
              value={categoryId}
              onChange={(e) => setCategoryId(e.target.value)}
              style={{ font: 'inherit', padding: '0.5rem', borderRadius: 4, border: '1px solid #ccc' }}
            >
              <option value="">Sin categoría</option>
              {categories.map((c) => (
                <option key={c.id} value={c.id}>{c.name}</option>
              ))}
            </select>
          </label>
          <label>
            Imagen (opcional)
            <input
              type="file"
              accept="image/*"
              onChange={(e) => setImage(e.target.files?.[0] ?? null)}
            />
          </label>
          <div className="admin-form-actions">
            <button type="submit" className="btn" disabled={saving}>
              {saving ? 'Creando...' : '✅ Crear producto'}
            </button>
            <Link to="/admin/productos" className="btn btn-secondary">
              Cancelar
            </Link>
          </div>
        </form>
      </section>
    </RequireAdmin>
  )
}