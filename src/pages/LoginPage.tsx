import { FormEvent, useState } from 'react'
import { Link, useNavigate, useSearchParams } from 'react-router-dom'
import { ADMIN_ROLE, useAuth } from '../context/AuthContext'

export function LoginPage() {
  const { login } = useAuth()
  const navigate = useNavigate()
  const [params] = useSearchParams()
  const next = params.get('next') ?? '/'

  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)

  async function onSubmit(e: FormEvent) {
    e.preventDefault()
    setError(null)
    setLoading(true)
    try {
      const role = await login(email, password)
      const dest = role === ADMIN_ROLE ? '/admin' : next.startsWith('/') ? next : `/${next}`
      navigate(dest, { replace: true })
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Error al entrar')
    } finally {
      setLoading(false)
    }
  }

  return (
    <>
      <h2>Iniciar sesión</h2>
      <p>
        ¿No tienes cuenta? <Link to="/register">Regístrate</Link>
      </p>
      {error && <p className="alert">{error}</p>}
      <form className="form" onSubmit={onSubmit}>
        <label>
          Email
          <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} required autoComplete="email" />
        </label>
        <label>
          Contraseña
          <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} required minLength={8} autoComplete="current-password" />
        </label>
        <button type="submit" className="btn" disabled={loading}>
          {loading ? 'Entrando…' : 'Entrar'}
        </button>
      </form>
    </>
  )
}
