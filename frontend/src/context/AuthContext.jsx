import { createContext, useContext, useEffect, useState } from 'react'
import apiClient from '../api/client'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const token = localStorage.getItem('token')
    if (!token) {
      setLoading(false)
      return
    }
    apiClient
      .get('/api/users/me')
      .then((res) => setUser(res.data))
      .catch(() => {
        localStorage.removeItem('token')
      })
      .finally(() => setLoading(false))
  }, [])

  async function login(username, password) {
    const res = await apiClient.post('/api/auth/login', { username, password })
    localStorage.setItem('token', res.data.token)
    setUser({ id: res.data.userId, username: res.data.username })
  }

  async function register(username, email, password) {
    const res = await apiClient.post('/api/auth/register', { username, email, password })
    localStorage.setItem('token', res.data.token)
    setUser({ id: res.data.userId, username: res.data.username })
  }

  function logout() {
    localStorage.removeItem('token')
    setUser(null)
  }

  return (
    <AuthContext.Provider value={{ user, loading, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  return useContext(AuthContext)
}
