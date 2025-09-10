import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { api } from '../api/client'
import { useAuth } from '../store/auth'

export function LoginPage() {
  const [usernameOrEmail, setUser] = useState('')
  const [password, setPass] = useState('')
  const [error, setError] = useState<string | null>(null)
  const navigate = useNavigate()
  const setSession = useAuth((s) => s.setSession)

  const submit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError(null)
    try {
      const res = await api.post('/users/login', { usernameOrEmail, password })
      setSession({ accessToken: res.data.accessToken, refreshToken: res.data.refreshToken, user: res.data.user })
      navigate('/')
    } catch (err: any) {
      setError(err?.response?.data?.message ?? 'Login failed')
    }
  }

  return (
    <div className="min-h-screen grid place-items-center bg-gradient-to-b from-white to-[#f6f9fb]">
      <form onSubmit={submit} className="w-full max-w-sm p-6 rounded-2xl shadow-sm border bg-white">
        <h1 className="text-2xl font-semibold text-[#0b3b5e] mb-1">Telepesa</h1>
        <p className="text-sm text-[#5b7083] mb-6">Sign in to your account</p>
        <label className="block text-sm text-[#0b3b5e] mb-1">Username or Email</label>
        <input value={usernameOrEmail} onChange={(e) => setUser(e.target.value)} required className="input" placeholder="you@bank.com" />
        <label className="block text-sm text-[#0b3b5e] mt-4 mb-1">Password</label>
        <input type="password" value={password} onChange={(e) => setPass(e.target.value)} required className="input" placeholder="••••••••" />
        {error && <div className="text-red-600 text-sm mt-3">{error}</div>}
        <button className="btn-primary mt-6 w-full">Sign In</button>
      </form>
    </div>
  )
}


