import React, { useEffect, useState } from 'react'
import { cn } from '../../lib/utils'
import type { Notification } from '../../api/notifications'

interface NotificationToastProps {
  notification: Notification | null
  onClose: () => void
  duration?: number
}

export function NotificationToast({
  notification,
  onClose,
  duration = 5000,
}: NotificationToastProps) {
  const [isVisible, setIsVisible] = useState(false)
  const [isLeaving, setIsLeaving] = useState(false)

  useEffect(() => {
    if (notification) {
      setIsVisible(true)
      setIsLeaving(false)

      // Auto-dismiss after duration
      const timer = setTimeout(() => {
        handleClose()
      }, duration)

      return () => clearTimeout(timer)
    } else {
      setIsVisible(false)
    }
  }, [notification, duration])

  const handleClose = () => {
    setIsLeaving(true)
    setTimeout(() => {
      setIsVisible(false)
      setIsLeaving(false)
      onClose()
    }, 300) // Match the leave animation duration
  }

  if (!notification || !isVisible) {
    return null
  }

  const getToastIcon = (type: Notification['type']) => {
    switch (type) {
      case 'success':
        return '✅'
      case 'warning':
        return '⚠️'
      case 'error':
        return '❌'
      default:
        return '📬'
    }
  }

  const getToastColors = (type: Notification['type']) => {
    switch (type) {
      case 'success':
        return 'bg-green-50 border-green-200 text-green-800 dark:bg-green-900/30 dark:border-green-800 dark:text-green-200'
      case 'warning':
        return 'bg-yellow-50 border-yellow-200 text-yellow-800 dark:bg-yellow-900/30 dark:border-yellow-800 dark:text-yellow-200'
      case 'error':
        return 'bg-red-50 border-red-200 text-red-800 dark:bg-red-900/30 dark:border-red-800 dark:text-red-200'
      default:
        return 'bg-blue-50 border-blue-200 text-blue-800 dark:bg-blue-900/30 dark:border-blue-800 dark:text-blue-200'
    }
  }

  return (
    <div
      className={cn(
        'fixed top-4 right-4 z-50 min-w-80 max-w-md',
        'transform transition-all duration-300 ease-in-out',
        isLeaving ? 'translate-x-full opacity-0 scale-95' : 'translate-x-0 opacity-100 scale-100'
      )}
    >
      <div
        className={cn(
          'rounded-financial-lg border shadow-financial-lg p-4',
          'backdrop-blur-sm bg-white/90 dark:bg-slate-800/90',
          getToastColors(notification.type)
        )}
      >
        <div className="flex items-start justify-between gap-3">
          <div className="flex items-start gap-3 flex-1 min-w-0">
            {/* Icon */}
            <div className="text-2xl flex-shrink-0 mt-0.5">{getToastIcon(notification.type)}</div>

            {/* Content */}
            <div className="flex-1 min-w-0">
              <div className="font-medium text-sm mb-1 truncate">{notification.title}</div>
              <div className="text-sm opacity-90 line-clamp-2">{notification.message}</div>

              {/* Action button */}
              {notification.actionText && notification.actionUrl && (
                <button
                  onClick={() => {
                    if (notification.actionUrl.startsWith('/')) {
                      window.location.href = notification.actionUrl
                    } else {
                      window.open(notification.actionUrl, '_blank')
                    }
                    handleClose()
                  }}
                  className="mt-2 text-xs font-medium underline hover:no-underline opacity-90 hover:opacity-100"
                >
                  {notification.actionText} →
                </button>
              )}
            </div>
          </div>

          {/* Close button */}
          <button
            onClick={handleClose}
            className="flex-shrink-0 p-1 rounded-full hover:bg-black/10 dark:hover:bg-white/10 transition-colors"
            title="Dismiss notification"
          >
            <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="m6 6 12 12m0-12L6 18"
              />
            </svg>
          </button>
        </div>

        {/* Progress bar */}
        <div className="mt-3 h-1 bg-black/10 dark:bg-white/10 rounded-full overflow-hidden">
          <div
            className="h-full bg-current rounded-full animate-shrink-width"
            style={{
              animationDuration: `${duration}ms`,
              animationTimingFunction: 'linear',
              animationFillMode: 'forwards',
            }}
          />
        </div>
      </div>
    </div>
  )
}

// CSS for the progress bar animation (add to your global CSS)
/*
@keyframes shrink-width {
  from {
    width: 100%;
  }
  to {
    width: 0%;
  }
}

.animate-shrink-width {
  animation-name: shrink-width;
}
*/
