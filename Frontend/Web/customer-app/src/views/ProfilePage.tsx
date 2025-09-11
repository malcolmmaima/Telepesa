import { useState, useEffect, useRef } from 'react'
import { useAuth } from '../store/auth'
import { Card } from '../components/ui/Card'
import { Button } from '../components/ui/Button'
import { Input } from '../components/ui/Input'
import { userApi, type UpdateProfileRequest, type ChangePasswordRequest } from '../api/user'
import { useAvatarImage } from '../hooks/useAvatarImage'

export function ProfilePage() {
  const { user, updateUser } = useAuth()
  const currentAvatarUrl = (user as any)?.avatarUrl || user?.profilePicture || null
  const { imageSrc: avatarImageSrc, isLoading: isLoadingAvatar, error: avatarError } = useAvatarImage(currentAvatarUrl)
  const fileInputRef = useRef<HTMLInputElement>(null)
  const [isEditing, setIsEditing] = useState(false)
  const [isChangingPassword, setIsChangingPassword] = useState(false)
  const [loading, setLoading] = useState(false)
  const [uploadingAvatar, setUploadingAvatar] = useState(false)
  const [message, setMessage] = useState<{ type: 'success' | 'error'; text: string } | null>(null)
  const [userSettings, setUserSettings] = useState({
    emailNotifications: true,
    smsNotifications: false,
  })

  // Profile form state
  const [profileForm, setProfileForm] = useState({
    firstName: user?.firstName || '',
    lastName: user?.lastName || '',
    email: user?.email || '',
    phoneNumber: user?.phoneNumber || '',
    dateOfBirth: user?.dateOfBirth || '',
  })

  // Password form state
  const [passwordForm, setPasswordForm] = useState({
    currentPassword: '',
    newPassword: '',
    confirmPassword: '',
  })

  // Load user settings on component mount
  useEffect(() => {
    const loadUserSettings = async () => {
      try {
        const settings = await userApi.getUserSettings()
        setUserSettings(settings)
      } catch (error) {
        console.error('Failed to load user settings:', error)
      }
    }
    loadUserSettings()
  }, [])

  const handleProfileUpdate = async (e: React.FormEvent) => {
    e.preventDefault()
    setLoading(true)
    setMessage(null)

    try {
      const profileData: UpdateProfileRequest = {
        firstName: profileForm.firstName,
        lastName: profileForm.lastName,
        email: profileForm.email,
        phoneNumber: profileForm.phoneNumber || undefined,
        dateOfBirth: profileForm.dateOfBirth || undefined,
      }
      
      const response = await userApi.updateProfile(profileData)
      updateUser(response.user)
      setMessage({ type: 'success', text: response.message || 'Profile updated successfully!' })
      setIsEditing(false)
    } catch (error: any) {
      setMessage({ type: 'error', text: error.message || 'Failed to update profile' })
    } finally {
      setLoading(false)
    }
  }

  const handlePasswordChange = async (e: React.FormEvent) => {
    e.preventDefault()

    if (passwordForm.newPassword !== passwordForm.confirmPassword) {
      setMessage({ type: 'error', text: 'New passwords do not match' })
      return
    }

    if (passwordForm.newPassword.length < 8) {
      setMessage({ type: 'error', text: 'Password must be at least 8 characters long' })
      return
    }

    setLoading(true)
    setMessage(null)

    try {
      const passwordData: ChangePasswordRequest = {
        currentPassword: passwordForm.currentPassword,
        newPassword: passwordForm.newPassword,
      }
      
      const response = await userApi.changePassword(passwordData)
      setMessage({ type: 'success', text: response.message || 'Password changed successfully!' })
      setIsChangingPassword(false)
      setPasswordForm({ currentPassword: '', newPassword: '', confirmPassword: '' })
    } catch (error: any) {
      setMessage({ type: 'error', text: error.message || 'Failed to change password' })
    } finally {
      setLoading(false)
    }
  }

  const handleAvatarUpload = async (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0]
    if (!file) return

    // Validate file type
    if (!file.type.startsWith('image/')) {
      setMessage({ type: 'error', text: 'Please select a valid image file' })
      return
    }

    // Validate file size (5MB max)
    if (file.size > 5 * 1024 * 1024) {
      setMessage({ type: 'error', text: 'Image must be smaller than 5MB' })
      return
    }

    setUploadingAvatar(true)
    setMessage(null)

    try {
      const response = await userApi.uploadAvatar(file)
      if (user) {
        updateUser({ profilePicture: response.avatarUrl })
      }
      setMessage({ 
        type: 'success', 
        text: response.message || 'Profile picture updated!'
      })
    } catch (error: any) {
      setMessage({ type: 'error', text: error.message || 'Failed to upload profile picture' })
    } finally {
      setUploadingAvatar(false)
      // Reset file input
      if (fileInputRef.current) {
        fileInputRef.current.value = ''
      }
    }
  }

  const handleSettingsChange = async (setting: string, value: boolean) => {
    try {
      const newSettings = { ...userSettings, [setting]: value }
      setUserSettings(newSettings)
      await userApi.updateUserSettings(newSettings)
      setMessage({ type: 'success', text: 'Settings updated successfully!' })
    } catch (error: any) {
      // Revert on error
      setUserSettings(prev => ({ ...prev, [setting]: !value }))
      setMessage({ type: 'error', text: 'Failed to update settings' })
    }
  }

  return (
    <div className="max-w-4xl mx-auto p-6 space-y-8">
      {/* Header */}
      <div>
        <h1 className="text-3xl font-bold text-financial-navy mb-2">Profile Settings</h1>
        <p className="text-financial-gray">
          Manage your account information and security preferences.
        </p>
      </div>

      {/* Status Message */}
      {message && (
        <div
          className={`p-4 rounded-financial border ${
            message.type === 'success'
              ? 'bg-green-50 border-green-200 text-green-800'
              : 'bg-red-50 border-red-200 text-red-800'
          }`}
        >
          <div className="flex items-center">
            <span className="mr-2">{message.type === 'success' ? '✅' : '⚠️'}</span>
            {message.text}
          </div>
        </div>
      )}

      {/* Profile Picture */}
      <Card title="Profile Picture" description="Upload and manage your profile picture">
        <div className="flex items-center space-x-6">
          <div className="flex-shrink-0">
            <div className="relative w-24 h-24 rounded-full bg-gradient-to-br from-financial-blue to-financial-navy flex items-center justify-center overflow-hidden">
              {avatarImageSrc ? (
                <img
                  src={avatarImageSrc}
                  alt="Profile"
                  className="w-full h-full object-cover"
                />
              ) : isLoadingAvatar ? (
                <div className="animate-spin rounded-full h-16 w-16 border-b-2 border-white"></div>
              ) : (
                <span className="text-2xl font-bold text-white">
                  {user?.firstName?.[0]}{user?.lastName?.[0]}
                </span>
              )}
              {avatarError && (
                <div className="absolute bottom-0 right-0 w-6 h-6 bg-red-500 rounded-full flex items-center justify-center text-xs text-white" title={avatarError}>
                  !
                </div>
              )}
            </div>
          </div>
          <div className="flex-1">
            <h4 className="font-medium text-financial-navy mb-2">
              {currentAvatarUrl ? 'Update' : 'Upload'} Profile Picture
            </h4>
            <p className="text-sm text-financial-gray mb-4">
              Choose a clear photo of yourself. Accepted formats: JPG, PNG, GIF (max 5MB)
            </p>
            <div className="flex items-center space-x-3">
              <Button
                variant="outline"
                onClick={() => fileInputRef.current?.click()}
                disabled={uploadingAvatar}
                className="flex items-center gap-2"
              >
                {uploadingAvatar ? '📤 Uploading...' : '📷 Choose Photo'}
              </Button>
              {currentAvatarUrl && (
                <Button
                  variant="ghost"
                  onClick={async () => {
                    try {
                      setUploadingAvatar(true)
                      const resp = await userApi.deleteAvatar()
                      updateUser({ profilePicture: '' })
                      setMessage({ type: 'success', text: resp.message })
                    } catch (err: any) {
                      setMessage({ type: 'error', text: err.message || 'Failed to remove profile picture' })
                    } finally {
                      setUploadingAvatar(false)
                    }
                  }}
                  disabled={uploadingAvatar}
                  className="text-red-600 hover:text-red-700"
                >
                  🗑️ Remove
                </Button>
              )}
            </div>
            <input
              ref={fileInputRef}
              type="file"
              accept="image/*"
              onChange={handleAvatarUpload}
              className="hidden"
            />
          </div>
        </div>
      </Card>

      {/* Profile Information */}
      <Card title="Personal Information" description="Your basic account details">
        {!isEditing ? (
          <div className="space-y-4">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-financial-gray mb-1">
                  First Name
                </label>
                <div className="text-financial-navy">{user?.firstName || 'Not set'}</div>
              </div>
              <div>
                <label className="block text-sm font-medium text-financial-gray mb-1">
                  Last Name
                </label>
                <div className="text-financial-navy">{user?.lastName || 'Not set'}</div>
              </div>
              <div>
                <label className="block text-sm font-medium text-financial-gray mb-1">
                  Email Address
                </label>
                <div className="text-financial-navy">{user?.email}</div>
              </div>
              <div>
                <label className="block text-sm font-medium text-financial-gray mb-1">
                  Phone Number
                </label>
                <div className="text-financial-navy">{user?.phoneNumber || 'Not set'}</div>
              </div>
              <div>
                <label className="block text-sm font-medium text-financial-gray mb-1">
                  Date of Birth
                </label>
                <div className="text-financial-navy">{user?.dateOfBirth || 'Not set'}</div>
              </div>
              <div>
                <label className="block text-sm font-medium text-financial-gray mb-1">
                  Username
                </label>
                <div className="text-financial-navy">{user?.username}</div>
              </div>
            </div>
            <Button onClick={() => setIsEditing(true)} className="mt-4">
              Edit Profile
            </Button>
          </div>
        ) : (
          <form onSubmit={handleProfileUpdate} className="space-y-4">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <Input
                label="First Name"
                value={profileForm.firstName}
                onChange={e => setProfileForm(prev => ({ ...prev, firstName: e.target.value }))}
                required
              />
              <Input
                label="Last Name"
                value={profileForm.lastName}
                onChange={e => setProfileForm(prev => ({ ...prev, lastName: e.target.value }))}
                required
              />
              <Input
                label="Email Address"
                type="email"
                value={profileForm.email}
                onChange={e => setProfileForm(prev => ({ ...prev, email: e.target.value }))}
                required
              />
              <Input
                label="Phone Number"
                type="tel"
                value={profileForm.phoneNumber}
                onChange={e => setProfileForm(prev => ({ ...prev, phoneNumber: e.target.value }))}
              />
              <Input
                label="Date of Birth"
                type="date"
                value={profileForm.dateOfBirth}
                onChange={e => setProfileForm(prev => ({ ...prev, dateOfBirth: e.target.value }))}
              />
            </div>
            <div className="flex space-x-3">
              <Button type="submit" loading={loading}>
                Save Changes
              </Button>
              <Button
                variant="outline"
                onClick={() => {
                  setIsEditing(false)
                  setProfileForm({
                    firstName: user?.firstName || '',
                    lastName: user?.lastName || '',
                    email: user?.email || '',
                    phoneNumber: user?.phoneNumber || '',
                    dateOfBirth: user?.dateOfBirth || '',
                  })
                }}
              >
                Cancel
              </Button>
            </div>
          </form>
        )}
      </Card>

      {/* Password Change */}
      <Card title="Change Password" description="Update your account password for security">
        {!isChangingPassword ? (
          <div className="space-y-4">
            <p className="text-financial-gray text-sm">
              It's good practice to change your password regularly to keep your account secure.
            </p>
            <Button onClick={() => setIsChangingPassword(true)}>Change Password</Button>
          </div>
        ) : (
          <form onSubmit={handlePasswordChange} className="space-y-4">
            <div className="max-w-md space-y-4">
              <Input
                label="Current Password"
                type="password"
                value={passwordForm.currentPassword}
                onChange={e =>
                  setPasswordForm(prev => ({ ...prev, currentPassword: e.target.value }))
                }
                required
              />
              <Input
                label="New Password"
                type="password"
                value={passwordForm.newPassword}
                onChange={e => setPasswordForm(prev => ({ ...prev, newPassword: e.target.value }))}
                required
                minLength={8}
              />
              <Input
                label="Confirm New Password"
                type="password"
                value={passwordForm.confirmPassword}
                onChange={e =>
                  setPasswordForm(prev => ({ ...prev, confirmPassword: e.target.value }))
                }
                required
                minLength={8}
              />
            </div>
            <div className="flex space-x-3">
              <Button type="submit" loading={loading}>
                Update Password
              </Button>
              <Button
                variant="outline"
                onClick={() => {
                  setIsChangingPassword(false)
                  setPasswordForm({ currentPassword: '', newPassword: '', confirmPassword: '' })
                }}
              >
                Cancel
              </Button>
            </div>
          </form>
        )}
      </Card>

      {/* Account Settings */}
      <Card title="Account Settings" description="Additional account preferences">
        <div className="space-y-4">
          <div className="flex items-center justify-between py-2">
            <div>
              <h4 className="font-medium text-financial-navy">Email Notifications</h4>
              <p className="text-sm text-financial-gray">Receive transaction and account updates</p>
            </div>
            <label className="relative inline-flex items-center cursor-pointer">
              <input 
                type="checkbox" 
                className="sr-only peer" 
                checked={userSettings.emailNotifications}
                onChange={(e) => handleSettingsChange('emailNotifications', e.target.checked)}
              />
              <div className="w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-financial-blue/20 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-financial-blue"></div>
            </label>
          </div>
          <div className="flex items-center justify-between py-2">
            <div>
              <h4 className="font-medium text-financial-navy">SMS Notifications</h4>
              <p className="text-sm text-financial-gray">Receive transaction alerts via SMS</p>
            </div>
            <label className="relative inline-flex items-center cursor-pointer">
              <input 
                type="checkbox" 
                className="sr-only peer" 
                checked={userSettings.smsNotifications}
                onChange={(e) => handleSettingsChange('smsNotifications', e.target.checked)}
              />
              <div className="w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-financial-blue/20 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-financial-blue"></div>
            </label>
          </div>
        </div>
      </Card>
    </div>
  )
}
