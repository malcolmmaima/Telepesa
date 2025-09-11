import { useState, useEffect } from 'react'
import { useAuth } from '../../store/auth'

interface BackendStatusProps {
  showDetails?: boolean
}

interface ServiceStatus {
  name: string
  url: string
  status: 'online' | 'offline' | 'error' | 'checking'
  lastChecked?: Date
  error?: string
}

export function BackendStatus({ showDetails = false }: BackendStatusProps) {
  const { accessToken, user } = useAuth()
  const [services, setServices] = useState<ServiceStatus[]>([
    { name: 'User Service', url: '/users/me/profile', status: 'checking' },
    { name: 'Notifications Service', url: '/notifications', status: 'checking' },
    { name: 'Accounts Service', url: '/accounts', status: 'checking' },
  ])

  const checkServiceStatus = async (service: ServiceStatus): Promise<ServiceStatus> => {
    try {
      const response = await fetch(`${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1'}${service.url}`, {
        method: 'GET',
        headers: {
          'Authorization': `Bearer ${accessToken}`,
          'Content-Type': 'application/json'
        }
      })

      const isAuthProtected = response.status === 401 || response.status === 403
      return {
        ...service,
        status: response.ok ? 'online' : isAuthProtected ? 'online' : 'error',
        lastChecked: new Date(),
        error: response.ok
          ? undefined
          : isAuthProtected
            ? 'Authenticated access required'
            : `HTTP ${response.status} ${response.statusText}`
      }
    } catch (error) {
      return {
        ...service,
        status: 'offline',
        lastChecked: new Date(),
        error: error instanceof Error ? error.message : 'Connection failed'
      }
    }
  }

  const checkAllServices = async () => {
    if (!accessToken || !user) return

    const updatedServices = await Promise.all(
      services.map(service => checkServiceStatus(service))
    )
    setServices(updatedServices)
  }

  useEffect(() => {
    if (accessToken && user) {
      checkAllServices()
      
      // Check every 30 seconds
      const interval = setInterval(checkAllServices, 30000)
      return () => clearInterval(interval)
    }
  }, [accessToken, user])

  const getStatusColor = (status: ServiceStatus['status']) => {
    switch (status) {
      case 'online': return 'text-green-600'
      case 'offline': return 'text-red-600'
      case 'error': return 'text-yellow-600'
      case 'checking': return 'text-gray-600'
      default: return 'text-gray-600'
    }
  }

  const getStatusIcon = (status: ServiceStatus['status']) => {
    switch (status) {
      case 'online': return '✅'
      case 'offline': return '❌'
      case 'error': return '⚠️'
      case 'checking': return '⏳'
      default: return '❓'
    }
  }

  if (!showDetails) {
    const hasIssues = services.some(s => s.status === 'offline' || s.status === 'error')
    if (!hasIssues) return null

    return (
      <div className="fixed bottom-4 right-4 bg-yellow-50 border border-yellow-200 rounded-lg p-3 shadow-lg max-w-sm">
        <div className="flex items-center gap-2">
          <span className="text-yellow-600">⚠️</span>
          <div>
            <div className="text-sm font-medium text-yellow-800">Backend Issues Detected</div>
            <div className="text-xs text-yellow-600">Some services may be unavailable</div>
          </div>
        </div>
      </div>
    )
  }

  return (
    <div className="bg-white dark:bg-slate-800 border border-gray-200 dark:border-slate-700 rounded-lg p-4 shadow-sm">
      <div className="flex items-center justify-between mb-3">
        <h3 className="text-sm font-medium text-gray-900 dark:text-white">Backend Services</h3>
        <button
          onClick={checkAllServices}
          className="text-xs text-blue-600 hover:text-blue-700 dark:text-blue-400 dark:hover:text-blue-300"
        >
          Refresh
        </button>
      </div>
      
      <div className="space-y-2">
        {services.map((service) => (
          <div key={service.name} className="flex items-center justify-between text-sm">
            <div className="flex items-center gap-2">
              <span>{getStatusIcon(service.status)}</span>
              <span className="text-gray-700 dark:text-gray-300">{service.name}</span>
            </div>
            <div className={`text-xs ${getStatusColor(service.status)}`}>
              {service.status}
              {service.error && (
                <div className="text-xs text-gray-500 mt-1 max-w-xs truncate" title={service.error}>
                  {service.error}
                </div>
              )}
            </div>
          </div>
        ))}
      </div>

      {services.some(s => s.lastChecked) && (
        <div className="mt-2 pt-2 border-t border-gray-100 dark:border-slate-600">
          <div className="text-xs text-gray-500 dark:text-gray-400">
            Last checked: {services[0]?.lastChecked?.toLocaleTimeString()}
          </div>
        </div>
      )}
      
      {!accessToken && (
        <div className="mt-2 pt-2 border-t border-gray-100 dark:border-slate-600">
          <div className="text-xs text-yellow-600 dark:text-yellow-400">
            ⚠️ Not authenticated - some checks skipped
          </div>
        </div>
      )}
    </div>
  )
}
