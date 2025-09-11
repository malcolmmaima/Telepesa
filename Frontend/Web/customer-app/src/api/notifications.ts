import { api } from './client'

// Notification interfaces
export interface Notification {
  id: string
  userId: string
  title: string
  message: string
  type: 'info' | 'success' | 'warning' | 'error'
  read: boolean
  priority: 'low' | 'medium' | 'high'
  createdAt: string
  updatedAt: string
  actionUrl?: string
  actionText?: string
}

export interface NotificationPreferences {
  emailNotifications: boolean
  smsNotifications: boolean
  pushNotifications: boolean
  transactionAlerts: boolean
  securityAlerts: boolean
  promotionalOffers: boolean
}

export interface NotificationsSummary {
  totalCount: number
  unreadCount: number
  notifications: Notification[]
}

// API functions for notifications
export const notificationsApi = {
  // Get all notifications for the current user
  async getNotifications(page = 1, limit = 20): Promise<NotificationsSummary> {
    try {
      console.log(`[Notifications API] Fetching notifications (page: ${page}, limit: ${limit})`)
      const response = await api.get(`/api/v1/notifications?page=${page}&limit=${limit}`)
      
      // Handle different response structures
      const data = response.data?.data || response.data
      console.log('[Notifications API] Get notifications response:', data)
      
      return {
        totalCount: data?.totalCount || data?.length || 0,
        unreadCount: data?.unreadCount || 0,
        notifications: Array.isArray(data?.notifications) ? data.notifications : 
                      Array.isArray(data) ? data : []
      }
    } catch (error: any) {
      console.error('[Notifications API] Error fetching notifications:', error)
      
      // Return mock data if API fails (for development)
      return {
        totalCount: 0,
        unreadCount: 0,
        notifications: []
      }
    }
  },

  // Get unread notifications count
  async getUnreadCount(): Promise<number> {
    try {
      console.log('[Notifications API] Fetching unread count')
      const response = await api.get('/api/v1/notifications/unread-count')
      
      const data = response.data?.data || response.data
      console.log('[Notifications API] Unread count response:', data)
      
      return data?.count || data?.unreadCount || 0
    } catch (error: any) {
      console.error('[Notifications API] Error fetching unread count:', error)
      return 0
    }
  },

  // Mark notification as read
  async markAsRead(notificationId: string): Promise<boolean> {
    try {
      console.log(`[Notifications API] Marking notification ${notificationId} as read`)
      await api.patch(`/api/v1/notifications/${notificationId}/read`)
      console.log('[Notifications API] Notification marked as read successfully')
      return true
    } catch (error: any) {
      console.error('[Notifications API] Error marking notification as read:', error)
      return false
    }
  },

  // Mark all notifications as read
  async markAllAsRead(): Promise<boolean> {
    try {
      console.log('[Notifications API] Marking all notifications as read')
      await api.patch('/api/v1/notifications/mark-all-read')
      console.log('[Notifications API] All notifications marked as read successfully')
      return true
    } catch (error: any) {
      console.error('[Notifications API] Error marking all notifications as read:', error)
      return false
    }
  },

  // Delete notification
  async deleteNotification(notificationId: string): Promise<boolean> {
    try {
      console.log(`[Notifications API] Deleting notification ${notificationId}`)
      await api.delete(`/api/v1/notifications/${notificationId}`)
      console.log('[Notifications API] Notification deleted successfully')
      return true
    } catch (error: any) {
      console.error('[Notifications API] Error deleting notification:', error)
      return false
    }
  },

  // Get notification preferences
  async getPreferences(): Promise<NotificationPreferences> {
    try {
      console.log('[Notifications API] Fetching notification preferences')
      const response = await api.get('/api/v1/notifications/preferences')
      
      const data = response.data?.data || response.data
      console.log('[Notifications API] Preferences response:', data)
      
      return {
        emailNotifications: data?.emailNotifications || false,
        smsNotifications: data?.smsNotifications || false,
        pushNotifications: data?.pushNotifications || false,
        transactionAlerts: data?.transactionAlerts || true,
        securityAlerts: data?.securityAlerts || true,
        promotionalOffers: data?.promotionalOffers || false,
        ...data
      }
    } catch (error: any) {
      console.error('[Notifications API] Error fetching preferences:', error)
      
      // Return default preferences if API fails
      return {
        emailNotifications: false,
        smsNotifications: false,
        pushNotifications: false,
        transactionAlerts: true,
        securityAlerts: true,
        promotionalOffers: false
      }
    }
  },

  // Update notification preferences
  async updatePreferences(preferences: Partial<NotificationPreferences>): Promise<boolean> {
    try {
      console.log('[Notifications API] Updating notification preferences:', preferences)
      await api.patch('/api/v1/notifications/preferences', preferences)
      console.log('[Notifications API] Preferences updated successfully')
      return true
    } catch (error: any) {
      console.error('[Notifications API] Error updating preferences:', error)
      return false
    }
  }
}
