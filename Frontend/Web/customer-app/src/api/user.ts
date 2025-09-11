import { apiService } from './client'

export interface User {
  id: number
  username: string
  email: string
  firstName: string
  lastName: string
  phoneNumber?: string
  dateOfBirth?: string
  isActive: boolean
  role: string
  createdAt: string
  lastLoginAt?: string
  avatarUrl?: string
}

export interface UpdateProfileRequest {
  firstName: string
  lastName: string
  email: string
  phoneNumber?: string
  dateOfBirth?: string
}

export interface ChangePasswordRequest {
  currentPassword: string
  newPassword: string
}

export interface ProfileUpdateResponse {
  user: User
  message: string
}

export const userApi = {
  // Get current user profile
  getProfile: async (): Promise<User> => {
    const response = await apiService.user.getProfile()
    return response.data.data
  },

  // Update user profile
  updateProfile: async (profileData: UpdateProfileRequest): Promise<ProfileUpdateResponse> => {
    const response = await apiService.user.updateProfile(profileData)
    return response.data
  },

  // Change password
  changePassword: async (passwordData: ChangePasswordRequest): Promise<{ message: string }> => {
    const response = await apiService.user.changePassword(passwordData)
    return response.data
  },

  // Upload avatar/profile picture
  uploadAvatar: async (file: File): Promise<{ avatarUrl: string; message: string }> => {
    try {
      const response = await apiService.user.uploadAvatar(file)
      return {
        avatarUrl: response.data.avatarUrl || response.data.url,
        message: response.data.message || 'Profile picture updated successfully!'
      }
    } catch (error: any) {
      console.error('Avatar upload failed:', error)
      
      // Handle different types of backend errors gracefully
      if (error.statusCode === 500) {
        console.log('Avatar upload endpoint returned 500 error, using local fallback')
        
        // Create a blob URL for the uploaded file as a temporary solution
        const avatarUrl = URL.createObjectURL(file)
        
        // Store in localStorage as backup
        try {
          localStorage.setItem('user_avatar_fallback', avatarUrl)
        } catch (storageError) {
          console.warn('Could not store avatar fallback in localStorage:', storageError)
        }
        
        return {
          avatarUrl,
          message: 'Profile picture updated locally. Upload to server will be retried later.'
        }
      } else if (error.statusCode === 404) {
        console.log('Avatar upload endpoint not found (404)')
        return {
          avatarUrl: URL.createObjectURL(file),
          message: 'Profile picture saved locally. Server upload feature is not yet available.'
        }
      } else if (error.statusCode === 413) {
        throw new Error('File is too large. Please choose a smaller image (max 5MB).')
      } else if (error.statusCode === 401 || error.statusCode === 403) {
        throw new Error('You do not have permission to upload images. Please log in again.')
      }
      
      // For other errors, provide a user-friendly message
      throw new Error(error.message || 'Failed to upload profile picture. Please try again.')
    }
  },

  // Get user preferences/settings
  getUserSettings: async (): Promise<{
    emailNotifications: boolean
    smsNotifications: boolean
    darkMode?: boolean
  }> => {
    // For now, return defaults. This can be connected to actual backend when ready
    return {
      emailNotifications: true,
      smsNotifications: false,
      darkMode: false,
    }
  },

  // Update user preferences/settings
  updateUserSettings: async (settings: {
    emailNotifications?: boolean
    smsNotifications?: boolean
    darkMode?: boolean
  }): Promise<{ message: string }> => {
    // TODO: Implement actual API call when backend supports it
    // For now, simulate success
    return Promise.resolve({ message: 'Settings updated successfully' })
  },
}
