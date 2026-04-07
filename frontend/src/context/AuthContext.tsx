import React, { createContext, useCallback, useContext, useMemo, useState } from 'react'
import * as api from '../api/client'

const TOKEN_KEY = 'tienda_token'
const EMAIL_KEY = 'tienda_email'

interface AuthState {
  token: string | null
  email: string | null
}

interface AuthContextValue extends AuthState {
  login: (email: string, password: string) => Promise<void>
  register: (email: string, password: string, fullName: string) => Promise<void>
  logout: () => void
  isAuthenticated: boolean
}

const AuthContext = createContext<AuthContextValue | null>(null)

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [token, setToken] = useState<string | null>(() => localStorage.getItem(TOKEN_KEY))
  const [email, setEmail] = useState<string | null>(() => localStorage.getItem(EMAIL_KEY))

  const login = useCallback(async (e: string, password: string) => {
    const res = await api.login(e, password)
    localStorage.setItem(TOKEN_KEY, res.token)
    localStorage.setItem(EMAIL_KEY, res.email)
    setToken(res.token)
    setEmail(res.email)
  }, [])

  const register = useCallback(async (e: string, password: string, fullName: string) => {
    const res = await api.register(e, password, fullName)
    localStorage.setItem(TOKEN_KEY, res.token)
    localStorage.setItem(EMAIL_KEY, res.email)
    setToken(res.token)
    setEmail(res.email)
  }, [])

  const logout = useCallback(() => {
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(EMAIL_KEY)
    setToken(null)
    setEmail(null)
  }, [])

  const value = useMemo(
    () => ({
      token,
      email,
      login,
      register,
      logout,
      isAuthenticated: !!token,
    }),
    [token, email, login, register, logout],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth dentro de AuthProvider')
  return ctx
}
