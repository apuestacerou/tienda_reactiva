import { useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { fetchCategories } from '../api/client'
import type { CategoryResponse } from '../api/types'
import { AdminProductForm } from '../components/AdminProductForm'
import { RequireAdmin } from '../components/RequireAdmin'

export function AdminProductNewPage() {
  const navigate = useNavigate()
  const [categories, setCategories] = useState<CategoryResponse[]>([])

  useEffect(() => {
    fetchCategories()
      .then(setCategories)
      .catch(() => setCategories([]))
  }, [])

  return (
    <RequireAdmin>
      <Link to="/admin" className="admin-back">
        ← Volver al panel
      </Link>
      <h2>Crear producto</h2>
      <p style={{ marginTop: 0, opacity: 0.85 }}>
        Completa el formulario y guarda. Luego puedes revisar el listado en{' '}
        <Link to="/admin/productos">productos creados</Link>.
      </p>

      <AdminProductForm
        editingProduct={null}
        categories={categories}
        onSaved={() => navigate('/admin/productos')}
      />
    </RequireAdmin>
  )
}
