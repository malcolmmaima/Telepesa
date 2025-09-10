import { useState } from 'react'
import { Card } from '../components/ui/Card'
import { Button } from '../components/ui/Button'
import { Input } from '../components/ui/Input'

export function SecurityPage() {
  const [isEnabling2FA, setIsEnabling2FA] = useState(false)
  const [twoFactorEnabled, setTwoFactorEnabled] = useState(false)

  return (
    <div className="max-w-4xl mx-auto p-6 space-y-8">
      <div>
        <h1 className="text-3xl font-bold text-financial-navy mb-2">Security Settings</h1>
        <p className="text-financial-gray">Manage your account security and privacy preferences.</p>
      </div>

      {/* Two-Factor Authentication */}
      <Card
        title="Two-Factor Authentication"
        description="Add an extra layer of security to your account"
      >
        <div className="space-y-4">
          <div className="flex items-center justify-between p-4 bg-blue-50 rounded-financial">
            <div>
              <h4 className="font-medium text-financial-navy">2FA Status</h4>
              <p className="text-sm text-financial-gray">
                {twoFactorEnabled
                  ? 'Two-factor authentication is enabled'
                  : 'Two-factor authentication is disabled'}
              </p>
            </div>
            <div className="flex items-center">
              <span
                className={`w-3 h-3 rounded-full mr-2 ${twoFactorEnabled ? 'bg-green-500' : 'bg-red-500'}`}
              ></span>
              <span
                className={`font-medium ${twoFactorEnabled ? 'text-green-600' : 'text-red-600'}`}
              >
                {twoFactorEnabled ? 'Enabled' : 'Disabled'}
              </span>
            </div>
          </div>

          {!twoFactorEnabled && !isEnabling2FA && (
            <Button onClick={() => setIsEnabling2FA(true)}>Enable 2FA</Button>
          )}

          {isEnabling2FA && (
            <div className="space-y-4 p-4 border rounded-financial">
              <p className="text-sm text-financial-gray">
                Scan this QR code with your authenticator app or enter the secret key manually.
              </p>
              <div className="bg-gray-100 p-8 rounded-financial text-center">
                <div className="text-6xl mb-4">📱</div>
                <p className="text-sm text-financial-gray">QR Code placeholder</p>
              </div>
              <Input label="Enter verification code from your app" placeholder="000000" />
              <div className="flex space-x-3">
                <Button
                  onClick={() => {
                    setTwoFactorEnabled(true)
                    setIsEnabling2FA(false)
                  }}
                >
                  Verify & Enable
                </Button>
                <Button variant="outline" onClick={() => setIsEnabling2FA(false)}>
                  Cancel
                </Button>
              </div>
            </div>
          )}

          {twoFactorEnabled && (
            <Button variant="outline" onClick={() => setTwoFactorEnabled(false)}>
              Disable 2FA
            </Button>
          )}
        </div>
      </Card>

      {/* Trusted Devices */}
      <Card title="Trusted Devices" description="Devices you've logged in from recently">
        <div className="space-y-3">
          {[
            {
              device: 'MacBook Pro',
              browser: 'Chrome 120',
              location: 'Nairobi, Kenya',
              lastUsed: '2 hours ago',
              current: true,
            },
            {
              device: 'iPhone 15',
              browser: 'Safari',
              location: 'Nairobi, Kenya',
              lastUsed: '1 day ago',
              current: false,
            },
            {
              device: 'Windows PC',
              browser: 'Firefox 121',
              location: 'Mombasa, Kenya',
              lastUsed: '1 week ago',
              current: false,
            },
          ].map((device, index) => (
            <div
              key={index}
              className="flex items-center justify-between p-4 border rounded-financial"
            >
              <div className="flex items-center space-x-3">
                <div className="text-2xl">
                  {device.device.includes('MacBook') && '💻'}
                  {device.device.includes('iPhone') && '📱'}
                  {device.device.includes('Windows') && '🖥️'}
                </div>
                <div>
                  <h4 className="font-medium text-financial-navy">{device.device}</h4>
                  <p className="text-sm text-financial-gray">
                    {device.browser} • {device.location}
                  </p>
                  <p className="text-xs text-financial-gray">Last used: {device.lastUsed}</p>
                </div>
              </div>
              <div className="text-right">
                {device.current ? (
                  <span className="px-2 py-1 bg-green-100 text-green-800 text-xs rounded-full">
                    Current
                  </span>
                ) : (
                  <Button variant="ghost" size="sm">
                    Remove
                  </Button>
                )}
              </div>
            </div>
          ))}
        </div>
      </Card>

      {/* Login Activity */}
      <Card title="Recent Login Activity" description="Your recent sign-in attempts">
        <div className="space-y-3">
          {[
            {
              status: 'Success',
              time: '2025-01-10 14:30',
              ip: '192.168.1.1',
              location: 'Nairobi, Kenya',
            },
            {
              status: 'Success',
              time: '2025-01-09 09:15',
              ip: '192.168.1.1',
              location: 'Nairobi, Kenya',
            },
            { status: 'Failed', time: '2025-01-08 23:45', ip: '203.45.67.89', location: 'Unknown' },
          ].map((activity, index) => (
            <div
              key={index}
              className="flex items-center justify-between p-3 bg-gray-50 rounded-financial"
            >
              <div className="flex items-center space-x-3">
                <span
                  className={`w-3 h-3 rounded-full ${activity.status === 'Success' ? 'bg-green-500' : 'bg-red-500'}`}
                ></span>
                <div>
                  <p className="font-medium text-financial-navy">{activity.status} Login</p>
                  <p className="text-sm text-financial-gray">{activity.location}</p>
                </div>
              </div>
              <div className="text-right">
                <p className="text-sm text-financial-navy">{activity.time}</p>
                <p className="text-xs text-financial-gray">{activity.ip}</p>
              </div>
            </div>
          ))}
        </div>
      </Card>

      {/* Privacy Settings */}
      <Card title="Privacy Settings" description="Control your data and privacy preferences">
        <div className="space-y-4">
          <div className="flex items-center justify-between py-2">
            <div>
              <h4 className="font-medium text-financial-navy">Data Analytics</h4>
              <p className="text-sm text-financial-gray">
                Help us improve by sharing anonymous usage data
              </p>
            </div>
            <label className="relative inline-flex items-center cursor-pointer">
              <input type="checkbox" className="sr-only peer" />
              <div className="w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-financial-blue/20 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-financial-blue"></div>
            </label>
          </div>
          <div className="flex items-center justify-between py-2">
            <div>
              <h4 className="font-medium text-financial-navy">Marketing Communications</h4>
              <p className="text-sm text-financial-gray">
                Receive promotional offers and product updates
              </p>
            </div>
            <label className="relative inline-flex items-center cursor-pointer">
              <input type="checkbox" className="sr-only peer" defaultChecked />
              <div className="w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-financial-blue/20 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-financial-blue"></div>
            </label>
          </div>
        </div>
      </Card>
    </div>
  )
}
