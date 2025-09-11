import { useState, useEffect, useCallback } from 'react'
import { notificationsApi } from '../api/notifications'

export const useNotifications = () => {
  const [unreadCount, setUnreadCount] = useState(0)
  const [lastFetch, setLastFetch] = useState<Date | null>(null)
  const [loading, setLoading] = useState(false)

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

  // Only fetch notifications on demand to avoid 403 errors
  // Auto-fetch is disabled until backend authentication is properly configured

  // Manual refresh function
  const refresh = useCallback(() => {
    fetchUnreadCount()
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

  return {
    unreadCount,
    loading,
    lastFetch,
    refresh,
    updateUnreadCount,
    decrementUnreadCount,
    clearUnreadCount
  }
}
