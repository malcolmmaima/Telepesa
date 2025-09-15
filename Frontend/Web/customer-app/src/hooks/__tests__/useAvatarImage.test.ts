import { describe, it, expect, vi, beforeEach } from 'vitest'
import { renderHook } from '@testing-library/react'
import { useAvatarImage } from '../useAvatarImage'

// Mock the auth store
vi.mock('../../store/auth', () => ({
  useAuth: vi.fn(() => ({
    accessToken: 'mock-token',
  })),
}))

// Mock fetch
global.fetch = vi.fn()
global.URL.createObjectURL = vi.fn(() => 'blob:mock-url')
global.URL.revokeObjectURL = vi.fn()

describe('useAvatarImage', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('returns null for no avatar URL', () => {
    const { result } = renderHook(() => useAvatarImage(null))

    expect(result.current.imageSrc).toBeNull()
    expect(result.current.isLoading).toBe(false)
    expect(result.current.error).toBeNull()
  })

  it('returns URL directly for blob URLs', () => {
    const blobUrl = 'blob:http://localhost/avatar'
    const { result } = renderHook(() => useAvatarImage(blobUrl))

    expect(result.current.imageSrc).toBe(blobUrl)
    expect(result.current.isLoading).toBe(false)
    expect(result.current.error).toBeNull()
  })

  it('returns URL directly for external URLs', () => {
    const externalUrl = 'https://example.com/avatar.jpg'
    const { result } = renderHook(() => useAvatarImage(externalUrl))

    expect(result.current.imageSrc).toBe(externalUrl)
    expect(result.current.isLoading).toBe(false)
    expect(result.current.error).toBeNull()
  })

  it('handles loading state', () => {
    const mockFetch = vi.mocked(fetch)
    mockFetch.mockImplementation(() => new Promise(() => {})) // Never resolves

    const { result } = renderHook(() => useAvatarImage('/api/avatar/123'))

    expect(result.current.isLoading).toBe(true)
    expect(result.current.imageSrc).toBeNull()
    expect(result.current.error).toBeNull()
  })

  it('handles successful image fetch', async () => {
    const mockFetch = vi.mocked(fetch)
    const mockBlob = new Blob(['image data'], { type: 'image/jpeg' })

    mockFetch.mockResolvedValue({
      ok: true,
      blob: () => Promise.resolve(mockBlob),
    } as Response)

    const { result } = renderHook(() => useAvatarImage('/api/avatar/123'))

    // Wait for async operations
    await new Promise(resolve => setTimeout(resolve, 0))

    expect(result.current.imageSrc).toBe('blob:mock-url')
    expect(result.current.isLoading).toBe(false)
    expect(result.current.error).toBeNull()
  })

  it('handles fetch error', async () => {
    const mockFetch = vi.mocked(fetch)

    mockFetch.mockResolvedValue({
      ok: false,
      status: 404,
      statusText: 'Not Found',
    } as Response)

    const { result } = renderHook(() => useAvatarImage('/api/avatar/123'))

    // Wait for async operations
    await new Promise(resolve => setTimeout(resolve, 0))

    expect(result.current.imageSrc).toBeNull()
    expect(result.current.isLoading).toBe(false)
    expect(result.current.error).toBe('Image not found')
  })
})
