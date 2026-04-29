import { FormEvent, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export function RegisterPage() {
  const { register } = useAuth()
  const navigate = useNavigate()

  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [fullName, setFullName] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)

  async function onSubmit(e: FormEvent) {
    e.preventDefault()
    setError(null)
    setLoading(true)
    try {
      await register(email, password, fullName)
      navigate('/checkout', { replace: true })
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Error al registrar')
    } finally {
      setLoading(false)
    }
  }

  return (
    <>
      <h2>Crear cuenta</h2>
      <p>
        ¿Ya tienes cuenta? <Link to="/login">Entrar</Link>
      </p>
      {error && <p className="alert">{error}</p>}
      <form className="form" onSubmit={onSubmit}>
        <label>
          Nombre
          <input type="text" value={fullName} onChange={(e) => setFullName(e.target.value)} />
        </label>
        <label>
          Email
          <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} required autoComplete="email" />
        </label>
        <label>
          Contraseña (mín. 8 caracteres)
          <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} required minLength={8} autoComplete="new-password" />
        </label>
        <button type="submit" className="btn" disabled={loading}>
          {loading ? 'Creando…' : 'Registrarme'}
        </button>
      </form>
    </>
  )
}
