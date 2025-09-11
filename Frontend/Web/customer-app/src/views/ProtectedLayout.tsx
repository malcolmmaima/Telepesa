import { Outlet, Navigate } from 'react-router-dom'
import { useAuth } from '../store/auth'
import { Navbar } from '../components/layout/Navbar'
import { NotificationToast } from '../components/ui/NotificationToast'
import { BackendStatus } from '../components/ui/BackendStatus'
import { useNotifications } from '../hooks/useNotifications'

export function ProtectedLayout() {
  const { isAuthenticated, isLoading } = useAuth()
  const { newNotification, dismissNewNotification } = useNotifications()

  // Show loading state while checking authentication
  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-center">
          <div className="w-8 h-8 bg-financial-gradient rounded-lg flex items-center justify-center mx-auto mb-4">
            <span className="text-white font-bold text-lg">T</span>
          </div>
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-financial-navy dark:border-slate-400 mx-auto"></div>
          <p className="text-financial-gray dark:text-slate-300 mt-2">Loading...</p>
        </div>
      </div>
    )
  }

  // Redirect to login if not authenticated
  if (!isAuthenticated) {
    return <Navigate to="/login" replace />
  }

  return (
    <div className="min-h-screen">
      <Navbar />
      <main className="pb-8">
        <Outlet />
      </main>
      
      {/* Real-time notification toast */}
      <NotificationToast 
        notification={newNotification}
        onClose={dismissNewNotification}
      />
      
      {/* Backend status indicator (only shows when there are issues) */}
      <BackendStatus showDetails={false} />
    </div>
  )
}
