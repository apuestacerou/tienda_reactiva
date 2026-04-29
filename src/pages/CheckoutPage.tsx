import { useState } from 'react'
import { Link, Navigate } from 'react-router-dom'
import { createOrder } from '../api/client'
import { useAuth } from '../context/AuthContext'
import { useCart } from '../context/CartContext'

export function CheckoutPage() {
  const { token, isAuthenticated } = useAuth()
  const { lines, total, clear } = useCart()
  const [done, setDone] = useState<{ orderId: string; total: number } | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)

  if (!isAuthenticated || !token) {
    return <Navigate to="/login?next=/checkout" replace />
  }

  if (lines.length === 0 && !done) {
    return (
      <>
        <h2>Checkout</h2>
        <p>Tu carrito está vacío.</p>
        <Link to="/">Volver al catálogo</Link>
      </>
    )
  }

  async function confirmar() {
    if (!token) return
    setError(null)
    setLoading(true)
    try {
      const items = lines.map((l) => ({ productId: l.productId, quantity: l.quantity }))
      const res = await createOrder(token, items)
      clear()
      setDone({ orderId: res.orderId, total: Number(res.totalAmount) })
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Error')
    } finally {
      setLoading(false)
    }
  }

  if (done) {
    return (
      <>
        <h2>¡Pedido registrado!</h2>
        <p>
          Número de pedido: <strong>{done.orderId}</strong>
        </p>
        <p>Total: ${done.total.toFixed(2)}</p>
        <Link to="/">Seguir comprando</Link>
      </>
    )
  }

  return (
    <>
      <h2>Confirmar pedido</h2>
      <p>Revisa tu pedido y confirma. Total: <strong>${total.toFixed(2)}</strong></p>
      <ul className="cart-list">
        {lines.map((l) => (
          <li key={l.productId}>
            <span>
              {l.name} × {l.quantity}
            </span>
            <span>${(l.price * l.quantity).toFixed(2)}</span>
          </li>
        ))}
      </ul>
      {error && <p className="alert">{error}</p>}
      <button type="button" className="btn" disabled={loading} onClick={() => void confirmar()}>
        {loading ? 'Enviando…' : 'Confirmar pedido'}
      </button>
    </>
  )
}
