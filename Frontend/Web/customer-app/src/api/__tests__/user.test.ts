import { describe, it, expect, vi, beforeEach } from 'vitest'
import { userApi } from '../user'

// Mock the entire client module
vi.mock('../client', () => ({
  apiService: {
    user: {
      getProfile: vi.fn(),
      updateProfile: vi.fn(),
      changePassword: vi.fn(),
      uploadAvatar: vi.fn(),
      deleteAvatar: vi.fn(),
    },
  },
}))

describe('User API', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    // Mock URL.createObjectURL for avatar tests
    global.URL.createObjectURL = vi.fn(() => 'blob:mock-url')
  })

  describe('getProfile', () => {
    it('fetches user profile successfully', async () => {
      const mockUser = {
        id: 1,
        username: 'johndoe',
        email: 'john@example.com',
        firstName: 'John',
        lastName: 'Doe',
        isActive: true,
        role: 'USER',
        createdAt: '2023-01-01T00:00:00Z',
      }

      const { apiService } = await import('../client')
      vi.mocked(apiService.user.getProfile).mockResolvedValue({
        data: { data: mockUser },
      } as any)

      const result = await userApi.getProfile()

      expect(apiService.user.getProfile).toHaveBeenCalled()
      expect(result).toEqual(mockUser)
    })
  })

  describe('updateProfile', () => {
    it('updates user profile successfully', async () => {
      const profileData = {
        firstName: 'Jane',
        lastName: 'Smith',
        email: 'jane@example.com',
      }

      const mockResponse = {
        user: { ...profileData, id: 1 },
        message: 'Profile updated successfully!',
      }

      const { apiService } = await import('../client')
      vi.mocked(apiService.user.updateProfile).mockResolvedValue({
        data: mockResponse,
      } as any)

      const result = await userApi.updateProfile(profileData)

      expect(apiService.user.updateProfile).toHaveBeenCalledWith(profileData)
      expect(result).toEqual(mockResponse)
    })

    it('handles 500 error gracefully', async () => {
      const profileData = {
        firstName: 'Jane',
        lastName: 'Smith',
        email: 'jane@example.com',
      }

      const error = { statusCode: 500 }
      const { apiService } = await import('../client')
      vi.mocked(apiService.user.updateProfile).mockRejectedValue(error)

      await expect(userApi.updateProfile(profileData)).rejects.toThrow(
        'Profile update service is temporarily unavailable'
      )
    })
  })

  describe('uploadAvatar', () => {
    it('handles 500 error with fallback', async () => {
      const file = new File([''], 'avatar.jpg', { type: 'image/jpeg' })
      const error = { statusCode: 500 }

      const { apiService } = await import('../client')
      vi.mocked(apiService.user.uploadAvatar).mockRejectedValue(error)

      const result = await userApi.uploadAvatar(file)

      expect(result.avatarUrl).toContain('blob:')
      expect(result.message).toContain('locally')
    })
  })
})
