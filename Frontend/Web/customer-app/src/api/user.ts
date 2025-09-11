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
    const response = await apiService.user.uploadAvatar(file)
    return response.data
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
