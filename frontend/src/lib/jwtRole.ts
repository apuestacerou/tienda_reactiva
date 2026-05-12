function payloadFromToken(token: string): Record<string, unknown> | null {
  const parts = token.split('.')
  if (parts.length < 2) return null
  try {
    const b64 = parts[1].replace(/-/g, '+').replace(/_/g, '/')
    const padded = b64 + '='.repeat((4 - (b64.length % 4)) % 4)
    const json = atob(padded)
    return JSON.parse(json) as Record<string, unknown>
  } catch {
    return null
  }
}

/** Lee el claim `role` del JWT (sin verificar firma; solo UI). */
export function parseRoleFromToken(token: string | null): string | null {
  if (!token) return null
  const payload = payloadFromToken(token)
  if (!payload) return null
  const role = payload.role
  return typeof role === 'string' ? role : null
}

/** `exp` del JWT en milisegundos desde epoch; null si no hay o es inválido. */
export function getJwtExpMs(token: string | null): number | null {
  if (!token) return null
  const payload = payloadFromToken(token)
  if (!payload) return null
  const exp = payload.exp
  if (typeof exp === 'number') return exp * 1000
  return null
}
