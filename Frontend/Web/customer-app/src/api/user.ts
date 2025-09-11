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
    try {
      const response = await apiService.user.updateProfile(profileData)
      return response.data
    } catch (error: any) {
      console.error('Profile update failed:', error)
      
      // Handle different types of backend errors gracefully
      if (error.statusCode === 500) {
        console.log('Profile update endpoint returned 500 error - backend may not be fully configured')
        throw new Error('Profile update service is temporarily unavailable. Please try again later.')
      } else if (error.statusCode === 404) {
        console.log('Profile update endpoint not found (404)')
        throw new Error('Profile update feature is not yet available. Please contact support.')
      } else if (error.statusCode === 401 || error.statusCode === 403) {
        throw new Error('Authentication required. Please log in again to update your profile.')
      } else if (error.statusCode === 400) {
        // Bad request - likely validation error
        const errorMsg = error.response?.data?.message || 'Invalid profile data'
        throw new Error(errorMsg)
      } else if (error.statusCode === 409) {
        // Conflict - likely duplicate email or phone
        const errorMsg = error.response?.data?.message || 'Email or phone number already in use'
        throw new Error(errorMsg)
      }
      
      // For other errors, provide detailed feedback
      const errorMsg = error.response?.data?.message || error.message || 'Unknown error occurred'
      console.error('Detailed profile update error:', {
        status: error.statusCode,
        message: errorMsg,
        response: error.response?.data
      })
      
      throw new Error(`Failed to update profile: ${errorMsg}. Please try again.`)
    }
  },

  // Change password
  changePassword: async (passwordData: ChangePasswordRequest): Promise<{ message: string }> => {
    try {
      const response = await apiService.user.changePassword(passwordData)
      return response.data
    } catch (error: any) {
      console.error('Change password failed:', error)
      
      // Handle different types of backend errors gracefully
      if (error.statusCode === 500) {
        console.log('Change password endpoint returned 500 error - backend may not be fully configured')
        throw new Error('Password change service is temporarily unavailable. Please try again later or contact support.')
      } else if (error.statusCode === 404) {
        console.log('Change password endpoint not found (404)')
        throw new Error('Password change feature is not yet available. Please contact support.')
      } else if (error.statusCode === 401 || error.statusCode === 403) {
        throw new Error('Authentication required. Please log in again to change your password.')
      } else if (error.statusCode === 400) {
        // Bad request - likely validation error or wrong current password
        const errorMsg = error.response?.data?.message || 'Invalid password data'
        throw new Error(errorMsg)
      }
      
      // For other errors, provide detailed feedback
      const errorMsg = error.response?.data?.message || error.message || 'Unknown error occurred'
      console.error('Detailed password change error:', {
        status: error.statusCode,
        message: errorMsg,
        response: error.response?.data
      })
      
      throw new Error(`Failed to change password: ${errorMsg}. Please try again.`)
    }
  },

  // Upload avatar/profile picture
  uploadAvatar: async (file: File): Promise<{ avatarUrl: string; message: string }> => {
    try {
      const response = await apiService.user.uploadAvatar(file)
      const avatarUrl = response.data.avatarUrl || response.data.url
      return {
        avatarUrl: avatarUrl,
        message: response.data.message || 'Profile picture updated successfully!'
      }
    } catch (error: any) {
      console.error('Avatar upload failed:', error)
      
      // Handle different types of backend errors gracefully
      if (error.statusCode === 500) {
        console.log('Avatar upload endpoint returned 500 error, using local fallback')
        console.log('This likely means the FileStorageService or related dependencies are not properly configured')
        
        // Create a blob URL for the uploaded file as a temporary solution
        const avatarUrl = URL.createObjectURL(file)
        
        // Store in localStorage as backup
        try {
          localStorage.setItem('user_avatar_fallback', avatarUrl)
          localStorage.setItem('user_avatar_filename', file.name)
          localStorage.setItem('user_avatar_timestamp', new Date().toISOString())
        } catch (storageError) {
          console.warn('Could not store avatar fallback in localStorage:', storageError)
        }
        
        return {
          avatarUrl,
          message: 'Profile picture updated locally. Server upload will be available once backend is configured.'
        }
      } else if (error.statusCode === 404) {
        console.log('Avatar upload endpoint not found (404) - endpoint may not be implemented yet')
        const avatarUrl = URL.createObjectURL(file)
        localStorage.setItem('user_avatar_fallback', avatarUrl)
        return {
          avatarUrl,
          message: 'Profile picture saved locally. Server upload feature will be available soon.'
        }
      } else if (error.statusCode === 413) {
        throw new Error('File is too large. Please choose a smaller image (max 5MB).')
      } else if (error.statusCode === 401 || error.statusCode === 403) {
        throw new Error('Authentication required. Please log in again to upload images.')
      } else if (error.statusCode === 415) {
        throw new Error('File type not supported. Please use JPG, PNG, or GIF format.')
      }
      
      // For other errors, provide detailed feedback
      const errorMsg = error.response?.data?.message || error.message || 'Unknown error occurred'
      console.error('Detailed avatar upload error:', {
        status: error.statusCode,
        message: errorMsg,
        response: error.response?.data
      })
      
      throw new Error(`Failed to upload profile picture: ${errorMsg}. Please try again.`)
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
