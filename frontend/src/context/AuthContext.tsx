import React, { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react'
import * as api from '../api/client'
import { getJwtExpMs, parseRoleFromToken } from '../lib/jwtRole'

const TOKEN_KEY = 'tienda_token'
const REFRESH_KEY = 'tienda_refresh'
const EMAIL_KEY = 'tienda_email'
const ROLE_KEY = 'tienda_role'

export const ADMIN_ROLE = 'ADMINISTRADOR'

function initialRole(): string | null {
  const stored = localStorage.getItem(ROLE_KEY)
  if (stored) return stored
  return parseRoleFromToken(localStorage.getItem(TOKEN_KEY))
}

interface AuthState {
  token: string | null
  refreshToken: string | null
  email: string | null
  role: string | null
}

interface AuthContextValue extends AuthState {
  login: (email: string, password: string) => Promise<string>
  register: (email: string, password: string, fullName: string) => Promise<void>
  logout: () => void
  isAuthenticated: boolean
  isAdmin: boolean
}

const AuthContext = createContext<AuthContextValue | null>(null)

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [token, setToken] = useState<string | null>(() => localStorage.getItem(TOKEN_KEY))
  const [refreshToken, setRefreshToken] = useState<string | null>(() => localStorage.getItem(REFRESH_KEY))
  const [email, setEmail] = useState<string | null>(() => localStorage.getItem(EMAIL_KEY))
  const [role, setRole] = useState<string | null>(initialRole)

  const persistSession = useCallback((res: { token: string; refreshToken: string; email: string; role: string }) => {
    localStorage.setItem(TOKEN_KEY, res.token)
    localStorage.setItem(REFRESH_KEY, res.refreshToken)
    localStorage.setItem(EMAIL_KEY, res.email)
    localStorage.setItem(ROLE_KEY, res.role)
    setToken(res.token)
    setRefreshToken(res.refreshToken)
    setEmail(res.email)
    setRole(res.role)
  }, [])

  const clearSession = useCallback(() => {
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(REFRESH_KEY)
    localStorage.removeItem(EMAIL_KEY)
    localStorage.removeItem(ROLE_KEY)
    setToken(null)
    setRefreshToken(null)
    setEmail(null)
    setRole(null)
  }, [])

  const login = useCallback(
    async (e: string, password: string) => {
      const res = await api.login(e, password)
      persistSession(res)
      return res.role
    },
    [persistSession],
  )

  const register = useCallback(
    async (e: string, password: string, fullName: string) => {
      const res = await api.register(e, password, fullName)
      persistSession(res)
    },
    [persistSession],
  )

  const tryRefreshAccess = useCallback(async () => {
    const rt = localStorage.getItem(REFRESH_KEY)
    if (!rt) return
    try {
      const res = await api.refreshAuth(rt)
      persistSession(res)
    } catch {
      clearSession()
    }
  }, [persistSession, clearSession])

  useEffect(() => {
    if (!token || !refreshToken) return
    const tick = () => {
      const exp = getJwtExpMs(token)
      if (exp == null) return
      if (exp - Date.now() < 120_000) void tryRefreshAccess()
    }
    const id = setInterval(tick, 60_000)
    tick()
    return () => clearInterval(id)
  }, [token, refreshToken, tryRefreshAccess])

  const logout = useCallback(() => {
    const rt = localStorage.getItem(REFRESH_KEY)
    if (rt) void api.logout(rt).catch(() => {})
    clearSession()
  }, [clearSession])

  const isAdmin = role === ADMIN_ROLE

  const value = useMemo(
    () => ({
      token,
      refreshToken,
      email,
      role,
      login,
      register,
      logout,
      isAuthenticated: !!token,
      isAdmin,
    }),
    [token, refreshToken, email, role, login, register, logout, isAdmin],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth dentro de AuthProvider')
  return ctx
}
