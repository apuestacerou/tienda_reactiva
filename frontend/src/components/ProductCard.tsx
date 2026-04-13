import type { ProductResponse } from '../api/types'

interface Props {
  product: ProductResponse
  onAdd: (p: ProductResponse) => void
}

export function ProductCard({ product, onAdd }: Props) {
  const disabled = product.stock <= 0
  return (
    <article className="card">
      {product.imageUrl ? (
        <img src={product.imageUrl} alt="" />
      ) : (
        <div style={{ height: 140, background: '#eee', borderRadius: 4 }} />
      )}
      <h3>{product.name}</h3>
      {product.categoryName ? (
        <p style={{ fontSize: '0.75rem', color: '#1565c0', margin: '0.15rem 0 0', fontWeight: 600 }}>
          {product.categoryName}
        </p>
      ) : null}
      <p style={{ fontSize: '0.85rem', color: '#555', margin: '0.25rem 0' }}>{product.description}</p>
      <p className="price">${product.price.toFixed(2)}</p>
      <p style={{ fontSize: '0.8rem', margin: '0.25rem 0' }}>Stock: {product.stock}</p>
      <button type="button" className="btn" disabled={disabled} onClick={() => onAdd(product)}>
        Añadir al carrito
      </button>
    </article>
  )
}
