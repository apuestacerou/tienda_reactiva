import { Link } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { useCart } from '../context/CartContext'

export function CartPage() {
  const { lines, remove, total, count } = useCart()
  const { isAuthenticated } = useAuth()

  return (
    <>
      <h2>Carrito</h2>
      {lines.length === 0 ? (
        <p>Tu carrito está vacío.</p>
      ) : (
        <>
          <ul className="cart-list">
            {lines.map((l) => (
              <li key={l.productId}>
                <span>
                  {l.name} × {l.quantity}
                </span>
                <span>
                  ${(l.price * l.quantity).toFixed(2)}{' '}
                  <button type="button" className="btn btn-secondary" style={{ marginLeft: 8 }} onClick={() => remove(l.productId)}>
                    Quitar
                  </button>
                </span>
              </li>
            ))}
          </ul>
          <p>
            <strong>Total: ${total.toFixed(2)}</strong> ({count} artículos)
          </p>
          <p className="alert">
            {isAuthenticated
              ? 'Puedes finalizar tu pedido.'
              : 'Para finalizar el pedido necesitas iniciar sesión o registrarte.'}
          </p>
          <Link className="btn" to="/checkout" style={{ display: 'inline-block', textDecoration: 'none' }}>
            Finalizar compra
          </Link>
        </>
      )}
    </>
  )
}
