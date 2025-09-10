import { Outlet, Navigate } from 'react-router-dom'
import { useAuth } from '../store/auth'
import { Navbar } from '../components/layout/Navbar'

export function ProtectedLayout() {
  const { isAuthenticated, isLoading } = useAuth()
  
  // Show loading state while checking authentication
  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-financial-background">
        <div className="text-center">
          <div className="w-8 h-8 bg-financial-gradient rounded-lg flex items-center justify-center mx-auto mb-4">
            <span className="text-white font-bold text-lg">T</span>
          </div>
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-financial-navy mx-auto"></div>
          <p className="text-financial-gray mt-2">Loading...</p>
        </div>
      </div>
    )
  }
  
  // Redirect to login if not authenticated
  if (!isAuthenticated) {
    return <Navigate to="/login" replace />
  }
  
  return (
    <div className="min-h-screen bg-financial-background">
      <Navbar />
      <main className="pb-8">
        <Outlet />
      </main>
    </div>
  )
}


