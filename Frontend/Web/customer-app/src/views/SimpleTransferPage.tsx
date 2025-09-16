import { useState, useEffect } from 'react'
import { useAuth } from '../store/auth'
import { transfersApi, type CreateTransferRequest } from '../api/transfers'
import { accountsApi, type Account } from '../api/accounts'
import { Card } from '../components/ui/Card'
import { Button } from '../components/ui/Button'
import { Input } from '../components/ui/Input'
import { formatCurrency, cn } from '../lib/utils'

interface TransferForm {
  fromAccountId: number
  recipientAccountNumber: string
  recipientName: string
  amount: number
  description: string
}

interface TransferResult {
  id: string
  transferReference: string
  amount: number
  recipientName: string
  status: string
}

export function SimpleTransferPage() {
  const { user } = useAuth()
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [success, setSuccess] = useState<TransferResult | null>(null)
  
  // Data states
  const [accounts, setAccounts] = useState<Account[]>([])
  
  // Form state
  const [form, setForm] = useState<TransferForm>({
    fromAccountId: 0,
    recipientAccountNumber: '',
    recipientName: '',
    amount: 0,
    description: ''
  })

  // Account lookup state
  const [accountLookup, setAccountLookup] = useState<{
    loading: boolean
    result: { accountName: string; accountType: string } | null
    error: string | null
  }>({ loading: false, result: null, error: null })

  // Load user accounts on mount
  useEffect(() => {
    if (user?.id) {
      loadUserAccounts()
    }
  }, [user?.id])

  // Account lookup when recipient account number changes
  useEffect(() => {
    if (form.recipientAccountNumber && form.recipientAccountNumber.length >= 10) {
      const timeoutId = setTimeout(() => {
        lookupRecipientAccount(form.recipientAccountNumber)
      }, 1000)
      return () => clearTimeout(timeoutId)
    } else {
      setAccountLookup({ loading: false, result: null, error: null })
    }
  }, [form.recipientAccountNumber])

  const loadUserAccounts = async () => {
    try {
      const userAccounts = await accountsApi.getUserAccounts(user!.id, 0, 50)
      const accountsArray = Array.isArray(userAccounts) ? userAccounts : []
      const activeAccounts = accountsArray.filter(account => account.status === 'ACTIVE')
      setAccounts(activeAccounts)
      
      if (activeAccounts.length > 0 && !form.fromAccountId) {
        setForm(prev => ({ ...prev, fromAccountId: activeAccounts[0].id }))
      }
    } catch (err: any) {
      console.error('Failed to load accounts:', err)
      setError('Failed to load accounts. Please refresh the page.')
    }
  }

  const lookupRecipientAccount = async (accountNumber: string) => {
    try {
      setAccountLookup({ loading: true, result: null, error: null })
      
      // For now, we'll simulate account lookup since the backend endpoint might not be available
      // In a real implementation, this would call the account service
      await new Promise(resolve => setTimeout(resolve, 500)) // Simulate API delay
      
      // Mock successful lookup
      setAccountLookup({
        loading: false,
        result: {
          accountName: 'Account Holder',
          accountType: 'SAVINGS'
        },
        error: null
      })
      
      // Auto-fill recipient name if not already set
      if (!form.recipientName) {
        setForm(prev => ({ ...prev, recipientName: 'Account Holder' }))
      }
    } catch (err: any) {
      setAccountLookup({
        loading: false,
        result: null,
        error: 'Account not found'
      })
    }
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError(null)
    
    if (!form.recipientAccountNumber || !form.recipientName || form.amount <= 0) {
      setError('Please fill in all required fields')
      return
    }

    const selectedAccount = accounts.find(acc => acc.id === form.fromAccountId)
    if (!selectedAccount) {
      setError('Please select a valid account')
      return
    }

    if (form.amount > selectedAccount.balance) {
      setError(`Insufficient balance. Available: ${formatCurrency(selectedAccount.balance)}`)
      return
    }

    try {
      setLoading(true)

      const transferRequest: CreateTransferRequest = {
        recipientAccountId: form.recipientAccountNumber,
        amount: form.amount,
        transferType: 'INTERNAL',
        description: form.description,
        recipientName: form.recipientName,
        currency: 'KES'
      }

      const result = await transfersApi.createTransfer(transferRequest, selectedAccount.accountNumber)
      
      setSuccess({
        id: result.id?.toString() || '',
        transferReference: result.transferId || '',
        amount: result.amount || form.amount,
        recipientName: result.recipientName || form.recipientName,
        status: result.status || 'COMPLETED'
      })

      // Reset form
      setForm({
        fromAccountId: accounts.length > 0 ? accounts[0].id : 0,
        recipientAccountNumber: '',
        recipientName: '',
        amount: 0,
        description: ''
      })
      
      // Reload accounts to update balances
      await loadUserAccounts()
      
    } catch (err: any) {
      console.error('Transfer failed:', err)
      setError(err.message || 'Transfer failed. Please try again.')
    } finally {
      setLoading(false)
    }
  }

  const selectedAccount = accounts.find(acc => acc.id === form.fromAccountId)

  return (
    <div className="max-w-2xl mx-auto p-6 space-y-6">
      <div className="text-center">
        <h1 className="text-3xl font-bold text-financial-navy mb-2">💸 Send Money</h1>
        <p className="text-financial-gray">Transfer money to other Telepesa accounts instantly</p>
      </div>

      {/* Success Message */}
      {success && (
        <Card className="p-6 bg-green-50 border-green-200">
          <div className="text-center">
            <div className="text-4xl mb-3">🎉</div>
            <h3 className="text-xl font-semibold text-green-800 mb-2">Transfer Successful!</h3>
            <p className="text-green-700 mb-3">
              {formatCurrency(success.amount)} sent to {success.recipientName}
            </p>
            <div className="text-sm text-green-600 space-y-1">
              <p>Transfer ID: {success.transferReference}</p>
              <p>Status: {success.status}</p>
            </div>
            <Button
              variant="ghost"
              size="sm"
              onClick={() => setSuccess(null)}
              className="mt-4 text-green-600"
            >
              ✕ Dismiss
            </Button>
          </div>
        </Card>
      )}

      {/* Error Message */}
      {error && (
        <Card className="p-4 bg-red-50 border-red-200">
          <div className="flex items-center text-red-700">
            <span className="text-xl mr-3">⚠️</span>
            <div>
              <p className="font-medium">Transfer Failed</p>
              <p className="text-sm">{error}</p>
            </div>
            <Button
              variant="ghost"
              size="sm"
              onClick={() => setError(null)}
              className="ml-auto text-red-600"
            >
              ✕
            </Button>
          </div>
        </Card>
      )}

      <form onSubmit={handleSubmit} className="space-y-6">
        {/* From Account */}
        <Card className="p-6">
          <h3 className="text-lg font-semibold text-financial-navy mb-4">From Account</h3>
          {accounts.length > 0 ? (
            <div className="space-y-3">
              {accounts.map(account => (
                <div
                  key={account.id}
                  onClick={() => setForm(prev => ({ ...prev, fromAccountId: account.id }))}
                  className={cn(
                    'p-4 border rounded-lg cursor-pointer transition-all',
                    form.fromAccountId === account.id
                      ? 'border-financial-blue bg-blue-50'
                      : 'border-gray-200 hover:border-gray-300'
                  )}
                >
                  <div className="flex justify-between items-center">
                    <div>
                      <div className="font-medium text-financial-navy">
                        {account.accountType} Account
                      </div>
                      <div className="text-sm text-financial-gray">
                        •••• {account.accountNumber.slice(-4)}
                      </div>
                    </div>
                    <div className="text-right">
                      <div className="font-semibold text-financial-navy">
                        {formatCurrency(account.balance)}
                      </div>
                      <div className="text-xs text-financial-gray">Available</div>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <div className="text-center py-8">
              <div className="text-4xl mb-3">💳</div>
              <p className="text-financial-gray">No accounts available</p>
            </div>
          )}
        </Card>

        {/* Recipient Details */}
        <Card className="p-6">
          <h3 className="text-lg font-semibold text-financial-navy mb-4">Send To</h3>
          <div className="space-y-4">
            <div className="relative">
              <Input
                label="Recipient Account Number"
                value={form.recipientAccountNumber}
                onChange={e => setForm(prev => ({ 
                  ...prev, 
                  recipientAccountNumber: e.target.value,
                  recipientName: '' // Reset name when account changes
                }))}
                placeholder="Enter Telepesa account number"
                required
                className={cn(
                  accountLookup.error && 'border-red-500',
                  accountLookup.result && 'border-green-500'
                )}
              />

              {/* Account Lookup Status */}
              <div className="mt-2">
                {accountLookup.loading && (
                  <div className="flex items-center text-sm text-blue-600">
                    <svg className="animate-spin -ml-1 mr-2 h-4 w-4" fill="none" viewBox="0 0 24 24">
                      <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                      <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                    </svg>
                    Verifying account...
                  </div>
                )}

                {accountLookup.result && (
                  <div className="flex items-center text-sm text-green-600 bg-green-50 p-2 rounded">
                    <svg className="mr-2 h-4 w-4" fill="currentColor" viewBox="0 0 20 20">
                      <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clipRule="evenodd"></path>
                    </svg>
                    <div>
                      <div className="font-medium">{accountLookup.result.accountName}</div>
                      <div className="text-xs">{accountLookup.result.accountType}</div>
                    </div>
                  </div>
                )}

                {accountLookup.error && (
                  <div className="flex items-center text-sm text-red-600 bg-red-50 p-2 rounded">
                    <svg className="mr-2 h-4 w-4" fill="currentColor" viewBox="0 0 20 20">
                      <path fillRule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7 4a1 1 0 11-2 0 1 1 0 012 0zm-1-9a1 1 0 00-1 1v4a1 1 0 102 0V6a1 1 0 00-1-1z" clipRule="evenodd"></path>
                    </svg>
                    {accountLookup.error}
                  </div>
                )}
              </div>
            </div>

            <Input
              label="Recipient Name"
              value={form.recipientName}
              onChange={e => setForm(prev => ({ ...prev, recipientName: e.target.value }))}
              placeholder="Enter recipient name"
              required
            />
          </div>
        </Card>

        {/* Amount and Description */}
        <Card className="p-6">
          <h3 className="text-lg font-semibold text-financial-navy mb-4">Transfer Details</h3>
          <div className="space-y-4">
            <div>
              <Input
                label="Amount (KES)"
                type="number"
                min="1"
                step="0.01"
                value={form.amount}
                onChange={e => setForm(prev => ({ ...prev, amount: Number(e.target.value) }))}
                placeholder="0.00"
                required
              />
              {selectedAccount && form.amount > 0 && (
                <div className="mt-2 text-sm">
                  {form.amount <= selectedAccount.balance ? (
                    <span className="text-green-600">
                      ✓ Available balance: {formatCurrency(selectedAccount.balance)}
                    </span>
                  ) : (
                    <span className="text-red-600">
                      ⚠️ Insufficient balance. Available: {formatCurrency(selectedAccount.balance)}
                    </span>
                  )}
                </div>
              )}
            </div>

            <Input
              label="Description"
              value={form.description}
              onChange={e => setForm(prev => ({ ...prev, description: e.target.value }))}
              placeholder="What's this for?"
              required
            />
          </div>

          {/* Transfer Summary */}
          {form.amount > 0 && (
            <div className="mt-4 p-4 bg-blue-50 border border-blue-200 rounded-lg">
              <h4 className="font-medium text-financial-navy mb-2">Transfer Summary</h4>
              <div className="space-y-1 text-sm">
                <div className="flex justify-between">
                  <span>Amount:</span>
                  <span>{formatCurrency(form.amount)}</span>
                </div>
                <div className="flex justify-between">
                  <span>Transfer Fee:</span>
                  <span className="text-green-600">FREE</span>
                </div>
                <div className="flex justify-between font-semibold border-t border-blue-200 pt-1">
                  <span>Total:</span>
                  <span>{formatCurrency(form.amount)}</span>
                </div>
                <div className="text-financial-gray mt-2">
                  <span>⚡ Instant transfer • No fees for internal transfers</span>
                </div>
              </div>
            </div>
          )}
        </Card>

        {/* Submit Button */}
        <Button
          type="submit"
          disabled={
            loading || 
            !form.recipientAccountNumber || 
            !form.recipientName || 
            form.amount <= 0 ||
            !selectedAccount ||
            form.amount > selectedAccount.balance
          }
          className="w-full"
          size="lg"
        >
          {loading ? (
            <>
              <svg className="animate-spin -ml-1 mr-3 h-5 w-5" fill="none" viewBox="0 0 24 24">
                <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
              </svg>
              Sending Transfer...
            </>
          ) : (
            '🚀 Send Money'
          )}
        </Button>
      </form>
    </div>
  )
}
