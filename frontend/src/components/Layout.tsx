import { Link } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export function Layout({ children }: { children: React.ReactNode }) {
  const { isAuthenticated, isAdmin, email, logout } = useAuth()

  return (
    <>
      <header className="app-header">
        <h1>
          <Link to="/" style={{ color: 'inherit', textDecoration: 'none' }}>
            Tienda reactiva
          </Link>
        </h1>
        <nav>
          <Link to="/">Catálogo</Link>
          <Link to="/cart">Carrito</Link>
          {isAdmin && (
            <Link to="/admin" style={{ fontWeight: 700 }}>
              Administración
            </Link>
          )}
          {isAuthenticated ? (
            <>
              <span style={{ opacity: 0.9 }}>{email}</span>
              <button type="button" className="btn btn-secondary" onClick={() => logout()}>
                Salir
              </button>
            </>
          ) : (
            <>
              <Link to="/login">Entrar</Link>
              <Link to="/register">Registro</Link>
            </>
          )}
        </nav>
      </header>
      <main>{children}</main>
    </>
  )
}
