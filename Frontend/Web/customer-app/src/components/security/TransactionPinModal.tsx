import { useState } from 'react'
import { Button } from '../ui/Button'
import { Input } from '../ui/Input'
import { Card } from '../ui/Card'

interface TransactionPinModalProps {
  isOpen: boolean
  onClose: () => void
  onSuccess: (pin: string) => void
  mode: 'verify' | 'create' | 'change'
  title?: string
  description?: string
}

export function TransactionPinModal({
  isOpen,
  onClose,
  onSuccess,
  mode,
  title,
  description,
}: TransactionPinModalProps) {
  const [pin, setPin] = useState('')
  const [confirmPin, setConfirmPin] = useState('')
  const [currentPin, setCurrentPin] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')
    setLoading(true)

    try {
      if (mode === 'create') {
        if (pin.length !== 4) {
          setError('PIN must be exactly 4 digits')
          return
        }
        if (pin !== confirmPin) {
          setError('PINs do not match')
          return
        }
        if (!/^\d{4}$/.test(pin)) {
          setError('PIN must contain only numbers')
          return
        }
      }

      if (mode === 'change') {
        if (!currentPin || currentPin.length !== 4) {
          setError('Please enter your current PIN')
          return
        }
        if (pin.length !== 4 || confirmPin.length !== 4) {
          setError('New PIN must be exactly 4 digits')
          return
        }
        if (pin !== confirmPin) {
          setError('New PINs do not match')
          return
        }
      }

      if (mode === 'verify') {
        if (pin.length !== 4) {
          setError('Please enter your 4-digit PIN')
          return
        }
      }

      // Simulate API call delay
      await new Promise(resolve => setTimeout(resolve, 1000))

      onSuccess(mode === 'change' ? `${currentPin}:${pin}` : pin)
      handleClose()
    } catch (err: any) {
      setError(err.message || 'An error occurred')
    } finally {
      setLoading(false)
    }
  }

  const handleClose = () => {
    setPin('')
    setConfirmPin('')
    setCurrentPin('')
    setError('')
    setLoading(false)
    onClose()
  }

  const getTitle = () => {
    if (title) return title
    switch (mode) {
      case 'create':
        return '🔐 Create Transaction PIN'
      case 'change':
        return '🔄 Change Transaction PIN'
      case 'verify':
        return '🔒 Enter Transaction PIN'
      default:
        return 'Transaction PIN'
    }
  }

  const getDescription = () => {
    if (description) return description
    switch (mode) {
      case 'create':
        return 'Create a 4-digit PIN to secure your transactions'
      case 'change':
        return 'Enter your current PIN and create a new one'
      case 'verify':
        return 'Enter your 4-digit PIN to authorize this transaction'
      default:
        return ''
    }
  }

  if (!isOpen) return null

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
      <Card className="w-full max-w-md">
        <div className="p-6">
          <div className="text-center mb-6">
            <h2 className="text-xl font-bold text-financial-navy mb-2">{getTitle()}</h2>
            <p className="text-financial-gray text-sm">{getDescription()}</p>
          </div>

          {error && (
            <div className="mb-4 p-3 bg-red-50 border border-red-200 rounded-lg">
              <p className="text-red-600 text-sm flex items-center">
                <span className="mr-2">⚠️</span>
                {error}
              </p>
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-4">
            {mode === 'change' && (
              <div>
                <Input
                  label="Current PIN"
                  type="password"
                  value={currentPin}
                  onChange={e => setCurrentPin(e.target.value.replace(/\D/g, '').slice(0, 4))}
                  placeholder="••••"
                  maxLength={4}
                  required
                  className="text-center text-2xl tracking-widest"
                />
              </div>
            )}

            <div>
              <Input
                label={
                  mode === 'verify'
                    ? 'Transaction PIN'
                    : mode === 'change'
                      ? 'New PIN'
                      : 'Create PIN'
                }
                type="password"
                value={pin}
                onChange={e => setPin(e.target.value.replace(/\D/g, '').slice(0, 4))}
                placeholder="••••"
                maxLength={4}
                required
                className="text-center text-2xl tracking-widest"
              />
            </div>

            {(mode === 'create' || mode === 'change') && (
              <div>
                <Input
                  label="Confirm PIN"
                  type="password"
                  value={confirmPin}
                  onChange={e => setConfirmPin(e.target.value.replace(/\D/g, '').slice(0, 4))}
                  placeholder="••••"
                  maxLength={4}
                  required
                  className="text-center text-2xl tracking-widest"
                />
              </div>
            )}

            <div className="flex gap-3 pt-4">
              <Button
                type="button"
                variant="outline"
                onClick={handleClose}
                disabled={loading}
                className="flex-1"
              >
                Cancel
              </Button>
              <Button type="submit" disabled={loading} className="flex-1">
                {loading ? (
                  <>
                    <svg
                      className="animate-spin -ml-1 mr-2 h-4 w-4"
                      fill="none"
                      viewBox="0 0 24 24"
                    >
                      <circle
                        className="opacity-25"
                        cx="12"
                        cy="12"
                        r="10"
                        stroke="currentColor"
                        strokeWidth="4"
                      ></circle>
                      <path
                        className="opacity-75"
                        fill="currentColor"
                        d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
                      ></path>
                    </svg>
                    Processing...
                  </>
                ) : mode === 'verify' ? (
                  'Authorize'
                ) : mode === 'create' ? (
                  'Create PIN'
                ) : (
                  'Change PIN'
                )}
              </Button>
            </div>
          </form>

          {mode === 'create' && (
            <div className="mt-4 p-3 bg-blue-50 border border-blue-200 rounded-lg">
              <p className="text-blue-700 text-xs">
                <strong>Security Tips:</strong>
                <br />• Use a unique 4-digit combination
                <br />• Don't use obvious numbers like 1234 or your birth year
                <br />• Keep your PIN confidential
              </p>
            </div>
          )}
        </div>
      </Card>
    </div>
  )
}
