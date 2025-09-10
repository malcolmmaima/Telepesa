import { useState } from 'react'
import { useAuth } from '../store/auth'
import { Card } from '../components/ui/Card'
import { Button } from '../components/ui/Button'
import { Input } from '../components/ui/Input'

export function ProfilePage() {
  const { user } = useAuth()
  const [isEditing, setIsEditing] = useState(false)
  const [isChangingPassword, setIsChangingPassword] = useState(false)
  const [loading, setLoading] = useState(false)
  const [message, setMessage] = useState<{ type: 'success' | 'error', text: string } | null>(null)

  // Profile form state
  const [profileForm, setProfileForm] = useState({
    firstName: user?.firstName || '',
    lastName: user?.lastName || '',
    email: user?.email || '',
    phoneNumber: user?.phoneNumber || '',
    dateOfBirth: user?.dateOfBirth || ''
  })

  // Password form state
  const [passwordForm, setPasswordForm] = useState({
    currentPassword: '',
    newPassword: '',
    confirmPassword: ''
  })

  const handleProfileUpdate = async (e: React.FormEvent) => {
    e.preventDefault()
    setLoading(true)
    setMessage(null)

    try {
      // TODO: Implement profile update API call
      // await updateProfile(profileForm)
      setMessage({ type: 'success', text: 'Profile updated successfully!' })
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
      // TODO: Implement password change API call
      // await changePassword(passwordForm.currentPassword, passwordForm.newPassword)
      setMessage({ type: 'success', text: 'Password changed successfully!' })
      setIsChangingPassword(false)
      setPasswordForm({ currentPassword: '', newPassword: '', confirmPassword: '' })
    } catch (error: any) {
      setMessage({ type: 'error', text: error.message || 'Failed to change password' })
    } finally {
      setLoading(false)
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
        <div className={`p-4 rounded-financial border ${
          message.type === 'success' 
            ? 'bg-green-50 border-green-200 text-green-800' 
            : 'bg-red-50 border-red-200 text-red-800'
        }`}>
          <div className="flex items-center">
            <span className="mr-2">
              {message.type === 'success' ? '✅' : '⚠️'}
            </span>
            {message.text}
          </div>
        </div>
      )}

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
                onChange={(e) => setProfileForm(prev => ({ ...prev, firstName: e.target.value }))}
                required
              />
              <Input
                label="Last Name"
                value={profileForm.lastName}
                onChange={(e) => setProfileForm(prev => ({ ...prev, lastName: e.target.value }))}
                required
              />
              <Input
                label="Email Address"
                type="email"
                value={profileForm.email}
                onChange={(e) => setProfileForm(prev => ({ ...prev, email: e.target.value }))}
                required
              />
              <Input
                label="Phone Number"
                type="tel"
                value={profileForm.phoneNumber}
                onChange={(e) => setProfileForm(prev => ({ ...prev, phoneNumber: e.target.value }))}
              />
              <Input
                label="Date of Birth"
                type="date"
                value={profileForm.dateOfBirth}
                onChange={(e) => setProfileForm(prev => ({ ...prev, dateOfBirth: e.target.value }))}
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
                    dateOfBirth: user?.dateOfBirth || ''
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
            <Button onClick={() => setIsChangingPassword(true)}>
              Change Password
            </Button>
          </div>
        ) : (
          <form onSubmit={handlePasswordChange} className="space-y-4">
            <div className="max-w-md space-y-4">
              <Input
                label="Current Password"
                type="password"
                value={passwordForm.currentPassword}
                onChange={(e) => setPasswordForm(prev => ({ ...prev, currentPassword: e.target.value }))}
                required
              />
              <Input
                label="New Password"
                type="password"
                value={passwordForm.newPassword}
                onChange={(e) => setPasswordForm(prev => ({ ...prev, newPassword: e.target.value }))}
                required
                minLength={8}
              />
              <Input
                label="Confirm New Password"
                type="password"
                value={passwordForm.confirmPassword}
                onChange={(e) => setPasswordForm(prev => ({ ...prev, confirmPassword: e.target.value }))}
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
              <input type="checkbox" className="sr-only peer" defaultChecked />
              <div className="w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-financial-blue/20 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-financial-blue"></div>
            </label>
          </div>
          <div className="flex items-center justify-between py-2">
            <div>
              <h4 className="font-medium text-financial-navy">SMS Notifications</h4>
              <p className="text-sm text-financial-gray">Receive transaction alerts via SMS</p>
            </div>
            <label className="relative inline-flex items-center cursor-pointer">
              <input type="checkbox" className="sr-only peer" />
              <div className="w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-financial-blue/20 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-financial-blue"></div>
            </label>
          </div>
        </div>
      </Card>
    </div>
  )
}
