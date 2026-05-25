import { FormEvent, useEffect, useState } from 'react'
import { adminCreateProduct, adminUpdateProduct } from '../api/client'
import type { CategoryResponse, ProductResponse } from '../api/types'
import { useAuth } from '../context/AuthContext'

type Props = {
  /** null = alta nueva; con valor = edición */
  editingProduct: ProductResponse | null
  categories: CategoryResponse[]
  onCancelEdit?: () => void
  onSaved: () => void | Promise<void>
}

export function AdminProductForm({ editingProduct, categories, onCancelEdit, onSaved }: Props) {
  const { token } = useAuth()
  const [formError, setFormError] = useState<string | null>(null)
  const [saving, setSaving] = useState(false)
  const [name, setName] = useState('')
  const [description, setDescription] = useState('')
  const [price, setPrice] = useState('')
  const [stock, setStock] = useState('')
  const [categoryId, setCategoryId] = useState('')
  const [image, setImage] = useState<File | null>(null)

  useEffect(() => {
    if (editingProduct) {
      setName(editingProduct.name)
      setDescription(editingProduct.description ?? '')
      setPrice(String(editingProduct.price))
      setStock(String(editingProduct.stock))
      setCategoryId(editingProduct.categoryId ?? '')
    } else {
      setName('')
      setDescription('')
      setPrice('')
      setStock('')
      setCategoryId('')
    }
    setImage(null)
    setFormError(null)
  }, [editingProduct])

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
      if (editingProduct) {
        await adminUpdateProduct(token, editingProduct.id, fields, image)
      } else {
        await adminCreateProduct(token, fields, image)
      }
      await onSaved()
    } catch (err) {
      setFormError(err instanceof Error ? err.message : 'Error al guardar')
    } finally {
      setSaving(false)
    }
  }

  const isEdit = Boolean(editingProduct)

  return (
    <section className="card admin-form-card" style={{ marginBottom: '1.5rem' }}>
      <h3 style={{ marginTop: 0 }}>{isEdit ? 'Editar producto' : 'Nuevo producto'}</h3>
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
          <select
            value={categoryId}
            onChange={(e) => setCategoryId(e.target.value)}
            style={{ font: 'inherit', padding: '0.5rem', borderRadius: 4, border: '1px solid #ccc' }}
          >
            <option value="">Sin categoría</option>
            {categories.map((c) => (
              <option key={c.id} value={c.id}>
                {c.name}
              </option>
            ))}
          </select>
        </label>
        <label>
          Imagen {isEdit ? '(opcional; vacío deja la actual)' : '(opcional)'}
          <input type="file" accept="image/*" onChange={(e) => setImage(e.target.files?.[0] ?? null)} />
        </label>
        <div className="admin-form-actions">
          <button type="submit" className="btn" disabled={saving}>
            {saving ? 'Guardando…' : isEdit ? 'Actualizar' : 'Crear producto'}
          </button>
          {isEdit && onCancelEdit && (
            <button type="button" className="btn btn-secondary" onClick={onCancelEdit} disabled={saving}>
              Cancelar edición
            </button>
          )}
        </div>
      </form>
    </section>
  )
}
