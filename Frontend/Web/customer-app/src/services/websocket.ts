import { useAuth } from '../store/auth'
import type { Notification } from '../api/notifications'

export interface NotificationEvent {
  type: 'notification'
  data: Notification
}

export interface WebSocketMessage {
  type: 'notification' | 'unread_count_update' | 'connection_status'
  data: any
}

class WebSocketService {
  private ws: WebSocket | null = null
  private reconnectAttempts = 0
  private maxReconnectAttempts = 5
  private reconnectInterval = 1000
  private listeners: { [key: string]: Array<(data: any) => void> } = {}
  private isConnecting = false
  private heartbeatInterval: NodeJS.Timeout | null = null
  private connectionUrl: string

  constructor() {
    // Build WebSocket URL from the current API base URL
    const apiBase = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1'
    const wsProtocol = apiBase.startsWith('https') ? 'wss' : 'ws'
    const wsUrl = apiBase.replace(/^https?/, wsProtocol).replace('/api/v1', '')
    this.connectionUrl = `${wsUrl}/ws/notifications`
  }

  connect() {
    if (this.ws?.readyState === WebSocket.OPEN || this.isConnecting) {
      return
    }

    try {
      const { accessToken, user } = useAuth.getState()

      if (!accessToken || !user) {
        console.log('WebSocket: No auth token or user, skipping connection')
        return
      }

      this.isConnecting = true
      console.log('WebSocket: Connecting to', this.connectionUrl)

      // Add auth token as query parameter
      const wsUrl = `${this.connectionUrl}?token=${encodeURIComponent(accessToken)}&userId=${user.id}`
      this.ws = new WebSocket(wsUrl)

      this.ws.onopen = this.onOpen.bind(this)
      this.ws.onmessage = this.onMessage.bind(this)
      this.ws.onclose = this.onClose.bind(this)
      this.ws.onerror = this.onError.bind(this)
    } catch (error) {
      console.error('WebSocket: Failed to create connection:', error)
      this.isConnecting = false
      this.scheduleReconnect()
    }
  }

  private onOpen(event: Event) {
    console.log('WebSocket: Connected successfully')
    this.isConnecting = false
    this.reconnectAttempts = 0

    // Start heartbeat
    this.startHeartbeat()

    // Notify listeners
    this.emit('connection_status', { connected: true })

    // Request initial unread count
    this.send({
      type: 'get_unread_count',
      data: {},
    })
  }

  private onMessage(event: MessageEvent) {
    try {
      const message: WebSocketMessage = JSON.parse(event.data)
      console.log('WebSocket: Received message:', message)

      // Handle different message types
      switch (message.type) {
        case 'notification':
          this.emit('notification', message.data)
          // Also update unread count
          this.emit('unread_count_increment', 1)
          break
        case 'unread_count_update':
          this.emit('unread_count_update', message.data)
          break
        case 'connection_status':
          this.emit('connection_status', message.data)
          break
        default:
          console.log('WebSocket: Unknown message type:', message.type)
      }
    } catch (error) {
      console.error('WebSocket: Failed to parse message:', error, event.data)
    }
  }

  private onClose(event: CloseEvent) {
    console.log('WebSocket: Connection closed', event.code, event.reason)
    this.isConnecting = false
    this.stopHeartbeat()

    this.emit('connection_status', { connected: false })

    // Attempt to reconnect unless it was a clean close or the endpoint doesn't exist (404)
    if (
      event.code !== 1000 &&
      event.code !== 1002 &&
      this.reconnectAttempts < this.maxReconnectAttempts
    ) {
      this.scheduleReconnect()
    } else if (event.code === 1002) {
      console.log('WebSocket: Endpoint not found, stopping reconnection attempts')
      this.reconnectAttempts = this.maxReconnectAttempts
    }
  }

  private onError(event: Event) {
    console.warn('WebSocket: Connection failed (WebSocket endpoint may not be implemented yet)')
    this.isConnecting = false
    this.reconnectAttempts = this.maxReconnectAttempts // Stop trying to reconnect
    this.emit('connection_status', { connected: false, error: true })
  }

  private scheduleReconnect() {
    if (this.reconnectAttempts >= this.maxReconnectAttempts) {
      console.log('WebSocket: Max reconnection attempts reached')
      return
    }

    this.reconnectAttempts++
    const delay = Math.min(this.reconnectInterval * Math.pow(2, this.reconnectAttempts - 1), 30000)

    console.log(`WebSocket: Scheduling reconnect attempt ${this.reconnectAttempts} in ${delay}ms`)

    setTimeout(() => {
      if (this.ws?.readyState !== WebSocket.OPEN) {
        this.connect()
      }
    }, delay)
  }

  private startHeartbeat() {
    this.stopHeartbeat()

    this.heartbeatInterval = setInterval(() => {
      if (this.ws?.readyState === WebSocket.OPEN) {
        this.send({ type: 'ping', data: {} })
      }
    }, 30000) // Send ping every 30 seconds
  }

  private stopHeartbeat() {
    if (this.heartbeatInterval) {
      clearInterval(this.heartbeatInterval)
      this.heartbeatInterval = null
    }
  }

  send(message: { type: string; data: any }) {
    if (this.ws?.readyState === WebSocket.OPEN) {
      try {
        this.ws.send(JSON.stringify(message))
      } catch (error) {
        console.error('WebSocket: Failed to send message:', error)
      }
    } else {
      console.warn('WebSocket: Cannot send message, connection not open')
    }
  }

  // Event listener management
  on(event: string, callback: (data: any) => void) {
    if (!this.listeners[event]) {
      this.listeners[event] = []
    }
    this.listeners[event].push(callback)
  }

  off(event: string, callback: (data: any) => void) {
    if (this.listeners[event]) {
      this.listeners[event] = this.listeners[event].filter(cb => cb !== callback)
    }
  }

  private emit(event: string, data: any) {
    if (this.listeners[event]) {
      this.listeners[event].forEach(callback => {
        try {
          callback(data)
        } catch (error) {
          console.error(`WebSocket: Error in event listener for ${event}:`, error)
        }
      })
    }
  }

  disconnect() {
    console.log('WebSocket: Disconnecting')
    this.stopHeartbeat()

    if (this.ws) {
      this.ws.close(1000, 'Client disconnect')
      this.ws = null
    }

    this.isConnecting = false
    this.reconnectAttempts = 0
  }

  isConnected(): boolean {
    return this.ws?.readyState === WebSocket.OPEN
  }

  getConnectionState(): string {
    if (!this.ws) return 'disconnected'

    switch (this.ws.readyState) {
      case WebSocket.CONNECTING:
        return 'connecting'
      case WebSocket.OPEN:
        return 'connected'
      case WebSocket.CLOSING:
        return 'closing'
      case WebSocket.CLOSED:
        return 'disconnected'
      default:
        return 'unknown'
    }
  }
}

// Singleton instance
export const wsService = new WebSocketService()

// React hook for using WebSocket in components
export function useWebSocket() {
  return {
    connect: () => wsService.connect(),
    disconnect: () => wsService.disconnect(),
    send: (message: { type: string; data: any }) => wsService.send(message),
    on: (event: string, callback: (data: any) => void) => wsService.on(event, callback),
    off: (event: string, callback: (data: any) => void) => wsService.off(event, callback),
    isConnected: () => wsService.isConnected(),
    getConnectionState: () => wsService.getConnectionState(),
  }
}
