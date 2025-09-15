import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { renderHook, act } from '@testing-library/react'
import { useNotifications } from '../useNotifications'

// Mock the API and WebSocket service
vi.mock('../../api/notifications', () => ({
  notificationsApi: {
    getUnreadCount: vi.fn(),
  },
}))

vi.mock('../../services/websocket', () => ({
  wsService: {
    on: vi.fn(),
    off: vi.fn(),
    connect: vi.fn(),
    disconnect: vi.fn(),
    isConnected: vi.fn(() => false),
  },
}))

// Mock browser Notification API
Object.defineProperty(window, 'Notification', {
  value: {
    permission: 'default',
    requestPermission: vi.fn(() => Promise.resolve('granted')),
  },
  writable: true,
})

describe('useNotifications', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  afterEach(() => {
    vi.clearAllMocks()
  })

  it('initializes with default state', () => {
    const { result } = renderHook(() => useNotifications())

    expect(result.current.unreadCount).toBe(0)
    expect(result.current.loading).toBe(false)
    expect(result.current.lastFetch).toBeNull()
    expect(result.current.isConnected).toBe(false)
    expect(result.current.notifications).toEqual([])
    expect(result.current.newNotification).toBeNull()
  })

  it('provides refresh function', () => {
    const { result } = renderHook(() => useNotifications())

    expect(typeof result.current.refresh).toBe('function')
  })

  it('provides unread count management functions', () => {
    const { result } = renderHook(() => useNotifications())

    expect(typeof result.current.updateUnreadCount).toBe('function')
    expect(typeof result.current.decrementUnreadCount).toBe('function')
    expect(typeof result.current.clearUnreadCount).toBe('function')
  })

  it('provides WebSocket management functions', () => {
    const { result } = renderHook(() => useNotifications())

    expect(typeof result.current.connectWebSocket).toBe('function')
    expect(typeof result.current.disconnectWebSocket).toBe('function')
  })

  it('updates unread count correctly', () => {
    const { result } = renderHook(() => useNotifications())

    act(() => {
      result.current.updateUnreadCount(5)
    })

    expect(result.current.unreadCount).toBe(5)
  })

  it('decrements unread count correctly', () => {
    const { result } = renderHook(() => useNotifications())

    act(() => {
      result.current.updateUnreadCount(3)
    })

    act(() => {
      result.current.decrementUnreadCount()
    })

    expect(result.current.unreadCount).toBe(2)
  })

  it('clears unread count', () => {
    const { result } = renderHook(() => useNotifications())

    act(() => {
      result.current.updateUnreadCount(5)
    })

    act(() => {
      result.current.clearUnreadCount()
    })

    expect(result.current.unreadCount).toBe(0)
  })

  it('dismisses new notification', () => {
    const { result } = renderHook(() => useNotifications())

    act(() => {
      result.current.dismissNewNotification()
    })

    expect(result.current.newNotification).toBeNull()
  })
})
