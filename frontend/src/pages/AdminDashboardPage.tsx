import { Link } from 'react-router-dom'
import { RequireAdmin } from '../components/RequireAdmin'

export function AdminDashboardPage() {
  return (
    <RequireAdmin>
      <h2>Panel de administración</h2>
      <p style={{ marginTop: 0, opacity: 0.85 }}>
        Elige una opción. El catálogo público está en <Link to="/">inicio</Link>.
      </p>

      <div className="admin-dashboard">
        <Link to="/admin/nuevo" className="admin-dashboard-link">
          <h3>Crear producto</h3>
          <p>Alta de un producto nuevo con el mismo formulario de siempre (nombre, precio, imagen, etc.).</p>
        </Link>
        <Link to="/admin/productos" className="admin-dashboard-link">
          <h3>Productos creados</h3>
          <p>Ver el listado, editar datos o imágenes y eliminar productos del catálogo.</p>
        </Link>
        <Link to="/admin/users">Usuarios</Link>
      </div>
    </RequireAdmin>
  )
}
