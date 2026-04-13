import React, { createContext, useCallback, useContext, useMemo, useState } from 'react'
import * as api from '../api/client'
import { parseRoleFromToken } from '../lib/jwtRole'

const TOKEN_KEY = 'tienda_token'
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
  const [email, setEmail] = useState<string | null>(() => localStorage.getItem(EMAIL_KEY))
  const [role, setRole] = useState<string | null>(initialRole)

  const login = useCallback(async (e: string, password: string) => {
    const res = await api.login(e, password)
    localStorage.setItem(TOKEN_KEY, res.token)
    localStorage.setItem(EMAIL_KEY, res.email)
    localStorage.setItem(ROLE_KEY, res.role)
    setToken(res.token)
    setEmail(res.email)
    setRole(res.role)
    return res.role
  }, [])

  const register = useCallback(async (e: string, password: string, fullName: string) => {
    const res = await api.register(e, password, fullName)
    localStorage.setItem(TOKEN_KEY, res.token)
    localStorage.setItem(EMAIL_KEY, res.email)
    localStorage.setItem(ROLE_KEY, res.role)
    setToken(res.token)
    setEmail(res.email)
    setRole(res.role)
  }, [])

  const logout = useCallback(() => {
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(EMAIL_KEY)
    localStorage.removeItem(ROLE_KEY)
    setToken(null)
    setEmail(null)
    setRole(null)
  }, [])

  const isAdmin = role === ADMIN_ROLE

  const value = useMemo(
    () => ({
      token,
      email,
      role,
      login,
      register,
      logout,
      isAuthenticated: !!token,
      isAdmin,
    }),
    [token, email, role, login, register, logout, isAdmin],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth dentro de AuthProvider')
  return ctx
}
