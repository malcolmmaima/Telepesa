import { useState, useEffect } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { notificationsApi, type Notification } from '../../api/notifications'
import { cn } from '../../lib/utils'

interface NotificationsDropdownProps {
  isOpen: boolean
  onClose: () => void
}

export const NotificationsDropdown = ({ isOpen, onClose }: NotificationsDropdownProps) => {
  const [notifications, setNotifications] = useState<Notification[]>([])
  const [unreadCount, setUnreadCount] = useState(0)
  const [loading, setLoading] = useState(false)
  const [loadingAction, setLoadingAction] = useState<string | null>(null)
  const [error, setError] = useState<string | null>(null)
  const navigate = useNavigate()

  // Load notifications when dropdown opens
  useEffect(() => {
    if (isOpen) {
      loadNotifications()
    }
  }, [isOpen])

  const loadNotifications = async () => {
    setLoading(true)
    setError(null)
    try {
      const response = await notificationsApi.getNotifications(1, 10)
      setNotifications(response.notifications)
      setUnreadCount(response.unreadCount)
    } catch (error: any) {
      console.error('Error loading notifications:', error)
      if (error.statusCode === 403) {
        setError('Notifications require authentication. Please try logging out and back in.')
      } else if (error.statusCode === 404) {
        setError('Notifications service not available yet.')
      } else {
        setError('Unable to load notifications. Please try again later.')
      }
    } finally {
      setLoading(false)
    }
  }

  const handleMarkAsRead = async (notification: Notification) => {
    if (notification.read) return

    setLoadingAction(notification.id)
    try {
      const success = await notificationsApi.markAsRead(notification.id)
      if (success) {
        setNotifications(prev =>
          prev.map(n => (n.id === notification.id ? { ...n, read: true } : n))
        )
        setUnreadCount(prev => Math.max(0, prev - 1))
      }
    } catch (error) {
      console.error('Error marking notification as read:', error)
    } finally {
      setLoadingAction(null)
    }
  }

  const handleMarkAllAsRead = async () => {
    if (unreadCount === 0) return

    setLoadingAction('mark-all')
    try {
      const success = await notificationsApi.markAllAsRead()
      if (success) {
        setNotifications(prev => prev.map(n => ({ ...n, read: true })))
        setUnreadCount(0)
      }
    } catch (error) {
      console.error('Error marking all notifications as read:', error)
    } finally {
      setLoadingAction(null)
    }
  }

  const handleNotificationClick = async (notification: Notification) => {
    // Mark as read if not already
    await handleMarkAsRead(notification)

    // Close dropdown
    onClose()

    // Navigate to action URL if provided
    if (notification.actionUrl) {
      if (notification.actionUrl.startsWith('/')) {
        navigate(notification.actionUrl)
      } else {
        window.open(notification.actionUrl, '_blank')
      }
    }
  }

  const formatTimeAgo = (dateString: string): string => {
    try {
      const date = new Date(dateString)
      const now = new Date()
      const diffInSeconds = Math.floor((now.getTime() - date.getTime()) / 1000)

      if (diffInSeconds < 60) return 'Just now'
      if (diffInSeconds < 3600) return `${Math.floor(diffInSeconds / 60)}m ago`
      if (diffInSeconds < 86400) return `${Math.floor(diffInSeconds / 3600)}h ago`
      if (diffInSeconds < 604800) return `${Math.floor(diffInSeconds / 86400)}d ago`

      return date.toLocaleDateString()
    } catch {
      return 'Unknown'
    }
  }

  const getNotificationIcon = (type: Notification['type']) => {
    const iconClasses = 'w-5 h-5 flex-shrink-0'
    switch (type) {
      case 'success':
        return (
          <div className="w-8 h-8 rounded-full bg-green-100 dark:bg-green-900/30 flex items-center justify-center">
            <svg
              className={`${iconClasses} text-green-600 dark:text-green-400`}
              fill="currentColor"
              viewBox="0 0 24 24"
            >
              <path d="M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z" />
            </svg>
          </div>
        )
      case 'warning':
        return (
          <div className="w-8 h-8 rounded-full bg-yellow-100 dark:bg-yellow-900/30 flex items-center justify-center">
            <svg
              className={`${iconClasses} text-yellow-600 dark:text-yellow-400`}
              fill="currentColor"
              viewBox="0 0 24 24"
            >
              <path d="M1 21h22L12 2 1 21zm12-3h-2v-2h2v2zm0-4h-2v-4h2v4z" />
            </svg>
          </div>
        )
      case 'error':
        return (
          <div className="w-8 h-8 rounded-full bg-red-100 dark:bg-red-900/30 flex items-center justify-center">
            <svg
              className={`${iconClasses} text-red-600 dark:text-red-400`}
              fill="currentColor"
              viewBox="0 0 24 24"
            >
              <path d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z" />
            </svg>
          </div>
        )
      default: // 'info'
        return (
          <div className="w-8 h-8 rounded-full bg-blue-100 dark:bg-blue-900/30 flex items-center justify-center">
            <svg
              className={`${iconClasses} text-blue-600 dark:text-blue-400`}
              fill="currentColor"
              viewBox="0 0 24 24"
            >
              <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-6h2v6zm0-8h-2V7h2v2z" />
            </svg>
          </div>
        )
    }
  }

  if (!isOpen) return null

  return (
    <>
      {/* Backdrop */}
      <div className="fixed inset-0 z-40" onClick={onClose} />

      {/* Dropdown */}
      <div className="absolute right-0 mt-2 w-96 bg-white dark:bg-slate-800 rounded-financial-lg shadow-financial-lg border border-gray-100 dark:border-slate-700 z-50 max-h-[32rem] flex flex-col">
        {/* Header */}
        <div className="flex items-center justify-between px-4 py-3 border-b border-gray-100 dark:border-slate-700">
          <h3 className="text-sm font-medium text-financial-navy dark:text-slate-200">
            Notifications{' '}
            {unreadCount > 0 && (
              <span className="ml-1 inline-flex items-center px-2 py-0.5 rounded-full text-xs bg-red-100 dark:bg-red-900/30 text-red-700 dark:text-red-300">
                {unreadCount} new
              </span>
            )}
          </h3>

          <div className="flex items-center gap-2">
            {unreadCount > 0 && (
              <button
                onClick={handleMarkAllAsRead}
                disabled={loadingAction === 'mark-all'}
                className="text-xs text-financial-navy dark:text-slate-300 hover:text-financial-navy/80 dark:hover:text-slate-200 disabled:opacity-50"
              >
                {loadingAction === 'mark-all' ? 'Marking...' : 'Mark all read'}
              </button>
            )}
            <button
              onClick={loadNotifications}
              disabled={loading}
              className="text-xs text-financial-navy dark:text-slate-300 hover:text-financial-navy/80 dark:hover:text-slate-200 disabled:opacity-50"
              title="Refresh notifications"
            >
              🔄 {loading ? 'Loading...' : 'Refresh'}
            </button>
            <button
              onClick={onClose}
              className="p-1 hover:bg-gray-100 dark:hover:bg-slate-700 rounded"
            >
              <svg
                className="w-4 h-4 text-financial-gray dark:text-slate-400"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="m6 6 12 12m0-12L6 18"
                />
              </svg>
            </button>
          </div>
        </div>

        {/* Content */}
        <div className="flex-1 overflow-y-auto">
          {loading ? (
            <div className="p-4 text-center">
              <div className="animate-spin rounded-full h-6 w-6 border-b-2 border-financial-navy dark:border-slate-400 mx-auto mb-2"></div>
              <p className="text-sm text-financial-gray dark:text-slate-400">
                Loading notifications...
              </p>
            </div>
          ) : error ? (
            <div className="p-6 text-center">
              <svg
                className="w-12 h-12 text-red-500 mx-auto mb-3"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={1}
                  d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
                />
              </svg>
              <p className="text-sm text-red-600 dark:text-red-400 mb-3">{error}</p>
              <button
                onClick={loadNotifications}
                disabled={loading}
                className="text-xs bg-red-50 dark:bg-red-900/20 text-red-600 dark:text-red-400 px-3 py-1 rounded hover:bg-red-100 dark:hover:bg-red-900/40 transition-colors disabled:opacity-50"
              >
                🔄 Try Again
              </button>
            </div>
          ) : notifications.length === 0 ? (
            <div className="p-6 text-center">
              <svg
                className="w-12 h-12 text-financial-gray dark:text-slate-500 mx-auto mb-3"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={1}
                  d="M15 17h5l-5 5v-5zM4 19h11M4 15h11M4 11h11M4 7h11"
                />
              </svg>
              <p className="text-sm text-financial-gray dark:text-slate-400">
                No notifications yet
              </p>
              <p className="text-xs text-financial-gray/70 dark:text-slate-500 mt-1">
                We'll notify you when something happens
              </p>
            </div>
          ) : (
            <div className="divide-y divide-gray-100 dark:divide-slate-700">
              {notifications.map(notification => (
                <div
                  key={notification.id}
                  onClick={() => handleNotificationClick(notification)}
                  className={cn(
                    'p-4 hover:bg-gray-50 dark:hover:bg-slate-700/50 cursor-pointer transition-colors',
                    !notification.read && 'bg-blue-50/50 dark:bg-blue-900/10'
                  )}
                >
                  <div className="flex items-start gap-3">
                    {getNotificationIcon(notification.type)}

                    <div className="flex-1 min-w-0">
                      <div className="flex items-start justify-between gap-2">
                        <h4
                          className={cn(
                            'text-sm font-medium truncate',
                            notification.read
                              ? 'text-financial-gray dark:text-slate-300'
                              : 'text-financial-navy dark:text-slate-200'
                          )}
                        >
                          {notification.title}
                        </h4>

                        <div className="flex items-center gap-2 flex-shrink-0">
                          <span className="text-xs text-financial-gray/70 dark:text-slate-500">
                            {formatTimeAgo(notification.createdAt)}
                          </span>
                          {!notification.read && (
                            <div className="w-2 h-2 bg-blue-500 rounded-full flex-shrink-0"></div>
                          )}
                        </div>
                      </div>

                      <p className="text-sm text-financial-gray dark:text-slate-400 mt-1 line-clamp-2">
                        {notification.message}
                      </p>

                      {notification.actionText && (
                        <div className="mt-2">
                          <span className="text-xs text-blue-600 dark:text-blue-400 font-medium">
                            {notification.actionText} →
                          </span>
                        </div>
                      )}
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Footer */}
        {notifications.length > 0 && (
          <div className="border-t border-gray-100 dark:border-slate-700 p-3">
            <Link
              to="/notifications" // We could create a full notifications page
              onClick={onClose}
              className="block text-center text-sm text-blue-600 dark:text-blue-400 hover:text-blue-700 dark:hover:text-blue-300 font-medium"
            >
              View all notifications
            </Link>
          </div>
        )}
      </div>
    </>
  )
}
