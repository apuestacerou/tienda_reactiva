import { Link, Navigate, useLocation } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export function RequireAdmin({ children }: { children: React.ReactNode }) {
  const { token, isAdmin } = useAuth()
  const location = useLocation()
  const next = encodeURIComponent(location.pathname + location.search)

  if (!token) {
    return <Navigate to={`/login?next=${next}`} replace />
  }
  if (!isAdmin) {
    return (
      <>
        <h2>Administración</h2>
        <p className="alert">
          Tu cuenta no tiene rol de administrador. Si debería, actualiza el rol en Neon y vuelve a iniciar sesión.
        </p>
        <p>
          <Link to="/">Volver al catálogo</Link>
        </p>
      </>
    )
  }
  return <>{children}</>
}
