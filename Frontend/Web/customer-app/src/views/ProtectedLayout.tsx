import { Navigate, Outlet } from 'react-router-dom'
import { useAuth } from '../store/auth'

export function ProtectedLayout() {
  const { accessToken } = useAuth()
  if (!accessToken) return <Navigate to="/login" replace />
  return <Outlet />
}


