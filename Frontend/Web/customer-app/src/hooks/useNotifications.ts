import { useState, useEffect, useCallback } from 'react'
import { notificationsApi, type Notification } from '../api/notifications'
import { wsService } from '../services/websocket'

export const useNotifications = () => {
  const [unreadCount, setUnreadCount] = useState(0)
  const [lastFetch, setLastFetch] = useState<Date | null>(null)
  const [loading, setLoading] = useState(false)
  const [isConnected, setIsConnected] = useState(false)
  const [notifications, setNotifications] = useState<Notification[]>([])
  const [newNotification, setNewNotification] = useState<Notification | null>(null)

  // WebSocket event handlers
  useEffect(() => {
    const handleNewNotification = (notification: Notification) => {
      console.log('Real-time notification received:', notification)
      setNotifications(prev => [notification, ...prev])
      setNewNotification(notification)
      setUnreadCount(prev => prev + 1)

      // Show browser notification if permission is granted
      if ('Notification' in window && Notification.permission === 'granted') {
        new Notification(notification.title, {
          body: notification.message,
          icon: '/favicon.ico', // You can customize the icon
          tag: notification.id, // Prevent duplicate notifications
        })
      }

      // Clear the new notification after 5 seconds
      setTimeout(() => setNewNotification(null), 5000)
    }

    const handleUnreadCountUpdate = (count: number) => {
      console.log('Unread count updated:', count)
      setUnreadCount(count)
    }

    const handleConnectionStatus = (status: { connected: boolean; error?: boolean }) => {
      console.log('WebSocket connection status:', status)
      setIsConnected(status.connected)

      if (status.connected) {
        // Connection established, fetch initial unread count
        fetchUnreadCount()
      }
    }

    // Set up WebSocket event listeners
    wsService.on('notification', handleNewNotification)
    wsService.on('unread_count_update', handleUnreadCountUpdate)
    wsService.on('connection_status', handleConnectionStatus)

    // Connect to WebSocket
    wsService.connect()
    setIsConnected(wsService.isConnected())

    // Request browser notification permission
    if ('Notification' in window && Notification.permission === 'default') {
      Notification.requestPermission().then(permission => {
        console.log('Notification permission:', permission)
      })
    }

    // Cleanup
    return () => {
      wsService.off('notification', handleNewNotification)
      wsService.off('unread_count_update', handleUnreadCountUpdate)
      wsService.off('connection_status', handleConnectionStatus)
    }
  }, [])

  // Fetch unread count
  const fetchUnreadCount = useCallback(async () => {
    try {
      setLoading(true)
      const count = await notificationsApi.getUnreadCount()
      setUnreadCount(count)
      setLastFetch(new Date())
    } catch (error) {
      console.error('Error fetching unread notifications count:', error)
    } finally {
      setLoading(false)
    }
  }, [])

  // Manual refresh function
  const refresh = useCallback(() => {
    fetchUnreadCount()
    // Also try to reconnect WebSocket if not connected
    if (!wsService.isConnected()) {
      wsService.connect()
    }
  }, [fetchUnreadCount])

  // Update count manually (for optimistic updates)
  const updateUnreadCount = useCallback((newCount: number) => {
    setUnreadCount(Math.max(0, newCount))
  }, [])

  // Decrease unread count by 1 (when marking as read)
  const decrementUnreadCount = useCallback(() => {
    setUnreadCount(prev => Math.max(0, prev - 1))
  }, [])

  // Clear all unread notifications
  const clearUnreadCount = useCallback(() => {
    setUnreadCount(0)
  }, [])

  // Dismiss new notification
  const dismissNewNotification = useCallback(() => {
    setNewNotification(null)
  }, [])

  // Connect/disconnect WebSocket
  const connectWebSocket = useCallback(() => {
    wsService.connect()
  }, [])

  const disconnectWebSocket = useCallback(() => {
    wsService.disconnect()
    setIsConnected(false)
  }, [])

  return {
    unreadCount,
    loading,
    lastFetch,
    isConnected,
    notifications,
    newNotification,
    refresh,
    updateUnreadCount,
    decrementUnreadCount,
    clearUnreadCount,
    dismissNewNotification,
    connectWebSocket,
    disconnectWebSocket,
  }
}
