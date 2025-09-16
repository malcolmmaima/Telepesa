import { useState, useEffect } from 'react'
import { useAuth } from '../store/auth'
import { transfersApi, type CreateTransferRequest } from '../api/transfers'
import { accountsApi, type Account } from '../api/accounts'
import { Card } from '../components/ui/Card'
import { Button } from '../components/ui/Button'
import { Input } from '../components/ui/Input'
import { formatCurrency, cn } from '../lib/utils'

type TransferType = 'INTERNAL' | 'SWIFT' | 'RTGS' | 'PESALINK' | 'MPESA'

interface TransferForm {
  fromAccountId: number
  transferType: TransferType
  recipientAccountNumber: string
  recipientName: string
  amount: number
  description: string
  currency: string
  // SWIFT fields
  swiftCode: string
  recipientBankName: string
  recipientBankAddress: string
  intermediaryBankSwift: string
  // RTGS fields
  sortCode: string
  // PesaLink fields
  pesalinkBankCode: string
  // M-Pesa fields
  mpesaNumber: string
}

interface TransferResult {
  id: string
  transferReference: string
  amount: number
  recipientName: string
  status: string
  fee: number
}

const TRANSFER_TYPES = [
  {
    id: 'INTERNAL' as TransferType,
    name: 'Telepesa Transfer',
    icon: '🏦',
    description: 'Send to other Telepesa accounts',
    fee: 'FREE',
    processingTime: 'Instant'
  },
  {
    id: 'PESALINK' as TransferType,
    name: 'PesaLink',
    icon: '🇰🇪',
    description: 'Send to any Kenyan bank account',
    fee: 'KES 25',
    processingTime: 'Instant'
  },
  {
    id: 'MPESA' as TransferType,
    name: 'M-Pesa',
    icon: '📱',
    description: 'Send to M-Pesa mobile number',
    fee: 'KES 15',
    processingTime: 'Instant'
  },
  {
    id: 'RTGS' as TransferType,
    name: 'RTGS',
    icon: '🏛️',
    description: 'Real-time gross settlement',
    fee: 'KES 500',
    processingTime: '2-4 hours'
  },
  {
    id: 'SWIFT' as TransferType,
    name: 'SWIFT',
    icon: '🌍',
    description: 'International bank transfer',
    fee: 'USD 25',
    processingTime: '1-3 business days'
  }
]

const KENYAN_BANKS = [
  { code: '01', name: 'Kenya Commercial Bank' },
  { code: '02', name: 'Standard Chartered Bank' },
  { code: '03', name: 'Barclays Bank of Kenya' },
  { code: '04', name: 'Bank of Baroda' },
  { code: '07', name: 'Commercial Bank of Africa' },
  { code: '09', name: 'Consolidated Bank of Kenya' },
  { code: '10', name: 'Cooperative Bank of Kenya' },
  { code: '11', name: 'Credit Bank' },
  { code: '12', name: 'Development Bank of Kenya' },
  { code: '17', name: 'Equity Bank' },
  { code: '25', name: 'National Bank of Kenya' },
  { code: '31', name: 'Postbank' },
  { code: '63', name: 'Diamond Trust Bank' },
  { code: '68', name: 'Sidian Bank' }
]

export function ComprehensiveTransferPage() {
  const { user } = useAuth()
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [success, setSuccess] = useState<TransferResult | null>(null)
  
  // Data states
  const [accounts, setAccounts] = useState<Account[]>([])
  
  // Form state
  const [form, setForm] = useState<TransferForm>({
    fromAccountId: 0,
    transferType: 'INTERNAL',
    recipientAccountNumber: '',
    recipientName: '',
    amount: 0,
    description: '',
    currency: 'KES',
    // SWIFT fields
    swiftCode: '',
    recipientBankName: '',
    recipientBankAddress: '',
    intermediaryBankSwift: '',
    // RTGS fields
    sortCode: '',
    // PesaLink fields
    pesalinkBankCode: '',
    // M-Pesa fields
    mpesaNumber: ''
  })

  // Load user accounts on mount
  useEffect(() => {
    if (user?.id) {
      loadUserAccounts()
    }
  }, [user?.id])

  // Update currency when transfer type changes
  useEffect(() => {
    if (form.transferType === 'SWIFT') {
      setForm(prev => ({ ...prev, currency: 'USD' }))
    } else {
      setForm(prev => ({ ...prev, currency: 'KES' }))
    }
  }, [form.transferType])

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

  const calculateFee = (transferType: TransferType, amount: number): number => {
    switch (transferType) {
      case 'INTERNAL':
        return 0
      case 'PESALINK':
        return 25
      case 'MPESA':
        return 15
      case 'RTGS':
        return 500
      case 'SWIFT':
        return 25 // USD
      default:
        return 0
    }
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError(null)
    
    // Validation based on transfer type
    if (!form.recipientName || form.amount <= 0) {
      setError('Please fill in all required fields')
      return
    }

    if (form.transferType === 'INTERNAL' && !form.recipientAccountNumber) {
      setError('Please enter recipient account number')
      return
    }

    if (form.transferType === 'MPESA' && !form.mpesaNumber) {
      setError('Please enter M-Pesa number')
      return
    }

    if (form.transferType === 'PESALINK' && (!form.recipientAccountNumber || !form.pesalinkBankCode)) {
      setError('Please enter account number and select bank')
      return
    }

    if (form.transferType === 'RTGS' && (!form.recipientAccountNumber || !form.sortCode)) {
      setError('Please enter account number and sort code')
      return
    }

    if (form.transferType === 'SWIFT' && (!form.recipientAccountNumber || !form.swiftCode || !form.recipientBankName)) {
      setError('Please fill in all SWIFT transfer details')
      return
    }

    const selectedAccount = accounts.find(acc => acc.id === form.fromAccountId)
    if (!selectedAccount) {
      setError('Please select a valid account')
      return
    }

    const fee = calculateFee(form.transferType, form.amount)
    const totalAmount = form.amount + fee

    if (totalAmount > selectedAccount.balance) {
      setError(`Insufficient balance. Available: ${formatCurrency(selectedAccount.balance)}`)
      return
    }

    try {
      setLoading(true)

      const transferRequest: CreateTransferRequest = {
        recipientAccountId: form.transferType === 'MPESA' ? form.mpesaNumber : form.recipientAccountNumber,
        amount: form.amount,
        transferType: form.transferType,
        description: form.description,
        recipientName: form.recipientName,
        currency: form.currency,
        // SWIFT fields
        swiftCode: form.swiftCode || undefined,
        recipientBankName: form.recipientBankName || undefined,
        recipientBankAddress: form.recipientBankAddress || undefined,
        intermediaryBankSwift: form.intermediaryBankSwift || undefined,
        // RTGS fields
        sortCode: form.sortCode || undefined,
        // PesaLink fields
        pesalinkBankCode: form.pesalinkBankCode || undefined,
        // M-Pesa fields
        mpesaNumber: form.mpesaNumber || undefined
      }

      const result = await transfersApi.createTransfer(transferRequest, selectedAccount.accountNumber)
      
      setSuccess({
        id: result.id?.toString() || '',
        transferReference: result.transferId || '',
        amount: result.amount || form.amount,
        recipientName: result.recipientName || form.recipientName,
        status: result.status || 'PROCESSING',
        fee: fee
      })

      // Reset form
      setForm({
        fromAccountId: accounts.length > 0 ? accounts[0].id : 0,
        transferType: 'INTERNAL',
        recipientAccountNumber: '',
        recipientName: '',
        amount: 0,
        description: '',
        currency: 'KES',
        swiftCode: '',
        recipientBankName: '',
        recipientBankAddress: '',
        intermediaryBankSwift: '',
        sortCode: '',
        pesalinkBankCode: '',
        mpesaNumber: ''
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
  const selectedTransferType = TRANSFER_TYPES.find(t => t.id === form.transferType)
  const fee = calculateFee(form.transferType, form.amount)
  const totalAmount = form.amount + fee

  const renderTransferTypeSpecificFields = () => {
    switch (form.transferType) {
      case 'INTERNAL':
        return (
          <Input
            label="Recipient Account Number"
            value={form.recipientAccountNumber}
            onChange={e => setForm(prev => ({ ...prev, recipientAccountNumber: e.target.value }))}
            placeholder="Enter Telepesa account number"
            required
          />
        )

      case 'MPESA':
        return (
          <Input
            label="M-Pesa Number"
            value={form.mpesaNumber}
            onChange={e => setForm(prev => ({ ...prev, mpesaNumber: e.target.value }))}
            placeholder="254XXXXXXXXX"
            required
          />
        )

      case 'PESALINK':
        return (
          <div className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-financial-navy mb-2">
                Select Bank
              </label>
              <select
                value={form.pesalinkBankCode}
                onChange={e => setForm(prev => ({ ...prev, pesalinkBankCode: e.target.value }))}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-financial-blue focus:border-transparent"
                required
              >
                <option value="">Select a bank</option>
                {KENYAN_BANKS.map(bank => (
                  <option key={bank.code} value={bank.code}>
                    {bank.name}
                  </option>
                ))}
              </select>
            </div>
            <Input
              label="Account Number"
              value={form.recipientAccountNumber}
              onChange={e => setForm(prev => ({ ...prev, recipientAccountNumber: e.target.value }))}
              placeholder="Enter account number"
              required
            />
          </div>
        )

      case 'RTGS':
        return (
          <div className="space-y-4">
            <Input
              label="Account Number"
              value={form.recipientAccountNumber}
              onChange={e => setForm(prev => ({ ...prev, recipientAccountNumber: e.target.value }))}
              placeholder="Enter account number"
              required
            />
            <Input
              label="Sort Code"
              value={form.sortCode}
              onChange={e => setForm(prev => ({ ...prev, sortCode: e.target.value }))}
              placeholder="Enter 6-digit sort code"
              required
            />
          </div>
        )

      case 'SWIFT':
        return (
          <div className="space-y-4">
            <Input
              label="Recipient Bank Name"
              value={form.recipientBankName}
              onChange={e => setForm(prev => ({ ...prev, recipientBankName: e.target.value }))}
              placeholder="Enter bank name"
              required
            />
            <Input
              label="SWIFT/BIC Code"
              value={form.swiftCode}
              onChange={e => setForm(prev => ({ ...prev, swiftCode: e.target.value }))}
              placeholder="Enter SWIFT code (e.g., KCBLKENX)"
              required
            />
            <Input
              label="Account Number/IBAN"
              value={form.recipientAccountNumber}
              onChange={e => setForm(prev => ({ ...prev, recipientAccountNumber: e.target.value }))}
              placeholder="Enter account number or IBAN"
              required
            />
            <Input
              label="Bank Address"
              value={form.recipientBankAddress}
              onChange={e => setForm(prev => ({ ...prev, recipientBankAddress: e.target.value }))}
              placeholder="Enter bank address"
              required
            />
            <Input
              label="Intermediary Bank SWIFT (Optional)"
              value={form.intermediaryBankSwift}
              onChange={e => setForm(prev => ({ ...prev, intermediaryBankSwift: e.target.value }))}
              placeholder="Enter intermediary bank SWIFT if required"
            />
          </div>
        )

      default:
        return null
    }
  }

  return (
    <div className="max-w-4xl mx-auto p-6 space-y-6">
      <div className="text-center">
        <h1 className="text-3xl font-bold text-financial-navy mb-2">💸 Send Money</h1>
        <p className="text-financial-gray">Transfer money anywhere in the world</p>
      </div>

      {/* Success Message */}
      {success && (
        <Card className="p-6 bg-green-50 border-green-200">
          <div className="text-center">
            <div className="text-4xl mb-3">🎉</div>
            <h3 className="text-xl font-semibold text-green-800 mb-2">Transfer Initiated!</h3>
            <p className="text-green-700 mb-3">
              {formatCurrency(success.amount)} sent to {success.recipientName}
            </p>
            <div className="text-sm text-green-600 space-y-1">
              <p>Transfer ID: {success.transferReference}</p>
              <p>Status: {success.status}</p>
              <p>Fee: {formatCurrency(success.fee)}</p>
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

        {/* Transfer Type Selection */}
        <Card className="p-6">
          <h3 className="text-lg font-semibold text-financial-navy mb-4">Transfer Method</h3>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {TRANSFER_TYPES.map(type => (
              <div
                key={type.id}
                onClick={() => setForm(prev => ({ ...prev, transferType: type.id }))}
                className={cn(
                  'p-4 border rounded-lg cursor-pointer transition-all',
                  form.transferType === type.id
                    ? 'border-financial-blue bg-blue-50'
                    : 'border-gray-200 hover:border-gray-300'
                )}
              >
                <div className="text-center">
                  <div className="text-3xl mb-2">{type.icon}</div>
                  <div className="font-medium text-financial-navy">{type.name}</div>
                  <div className="text-xs text-financial-gray mb-2">{type.description}</div>
                  <div className="text-xs">
                    <span className="text-green-600 font-medium">Fee: {type.fee}</span>
                    <br />
                    <span className="text-blue-600">⏱️ {type.processingTime}</span>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </Card>

        {/* Recipient Details */}
        <Card className="p-6">
          <h3 className="text-lg font-semibold text-financial-navy mb-4">
            {selectedTransferType?.icon} Send To ({selectedTransferType?.name})
          </h3>
          <div className="space-y-4">
            {renderTransferTypeSpecificFields()}
            
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
                label={`Amount (${form.currency})`}
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
                  {totalAmount <= selectedAccount.balance ? (
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
                  <span>{formatCurrency(form.amount, form.currency)}</span>
                </div>
                <div className="flex justify-between">
                  <span>Transfer Fee:</span>
                  <span className={fee === 0 ? 'text-green-600' : ''}>
                    {fee === 0 ? 'FREE' : formatCurrency(fee, form.currency)}
                  </span>
                </div>
                <div className="flex justify-between font-semibold border-t border-blue-200 pt-1">
                  <span>Total:</span>
                  <span>{formatCurrency(totalAmount, form.currency)}</span>
                </div>
                <div className="text-financial-gray mt-2">
                  <span>⏱️ Processing time: {selectedTransferType?.processingTime}</span>
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
            !form.recipientName || 
            form.amount <= 0 ||
            !selectedAccount ||
            totalAmount > selectedAccount.balance
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
              Processing Transfer...
            </>
          ) : (
            `🚀 Send ${formatCurrency(totalAmount, form.currency)}`
          )}
        </Button>
      </form>
    </div>
  )
}
