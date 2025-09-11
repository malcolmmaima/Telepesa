import { useState, useEffect } from 'react'
import { useAuth } from '../store/auth'
import {
  transfersApi,
  type Transfer,
  type CreateTransferRequest,
  type SavedRecipient,
  type TransferFeeResponse,
  type BankInfo,
} from '../api/transfers'
import { accountsApi, type Account } from '../api/accounts'
import { Card } from '../components/ui/Card'
import { Button } from '../components/ui/Button'
import { Input } from '../components/ui/Input'
import { formatCurrency, cn } from '../lib/utils'

const TRANSFER_TYPE_ICONS = {
  INTERNAL: '🏦',
  EXTERNAL: '🌍',
  BANK_TRANSFER: '🏛️',
  MOBILE_MONEY: '📱',
}

const TRANSFER_STATUS_COLORS = {
  PENDING: 'text-yellow-600 bg-yellow-50 border-yellow-200',
  PROCESSING: 'text-blue-600 bg-blue-50 border-blue-200',
  COMPLETED: 'text-green-600 bg-green-50 border-green-200',
  FAILED: 'text-red-600 bg-red-50 border-red-200',
  CANCELLED: 'text-gray-600 bg-gray-50 border-gray-200',
}

type TabType = 'new-transfer' | 'history' | 'recipients'

export function TransferPage() {
  const { user } = useAuth()
  const [activeTab, setActiveTab] = useState<TabType>('new-transfer')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  // Data states
  const [accounts, setAccounts] = useState<Account[]>([])
  const [transfers, setTransfers] = useState<Transfer[]>([])
  const [savedRecipients, setSavedRecipients] = useState<SavedRecipient[]>([])
  const [banks, setBanks] = useState<BankInfo[]>([])

  // Form states
  const [transferForm, setTransferForm] = useState<CreateTransferRequest>({
    fromAccountId: 0,
    recipientName: '',
    amount: 0,
    transferType: 'INTERNAL',
    description: '',
    saveRecipient: false,
  })

  const [feeInfo, setFeeInfo] = useState<TransferFeeResponse | null>(null)
  const [selectedRecipient, setSelectedRecipient] = useState<SavedRecipient | null>(null)
  const [showConfirmation, setShowConfirmation] = useState(false)
  const [transferSuccess, setTransferSuccess] = useState<Transfer | null>(null)
  const [accountLookup, setAccountLookup] = useState<{
    loading: boolean
    result: { accountName: string; accountType: string; bankName?: string } | null
    error: string | null
  }>({ loading: false, result: null, error: null })

  // Load initial data
  useEffect(() => {
    if (user?.id) {
      loadUserAccounts()
      loadSavedRecipients()
      loadSupportedBanks()
    }
  }, [user?.id])

  // Load transfer history when tab is active
  useEffect(() => {
    if (user?.id && activeTab === 'history') {
      loadTransferHistory()
    }
  }, [user?.id, activeTab])

  const loadUserAccounts = async () => {
    try {
      const userAccounts = await accountsApi.getUserActiveAccounts(user!.id)
      // Ensure we always get an array
      const accountsArray = Array.isArray(userAccounts) ? userAccounts : []
      setAccounts(accountsArray)
      if (accountsArray.length > 0 && !transferForm.fromAccountId) {
        setTransferForm(prev => ({ ...prev, fromAccountId: accountsArray[0].id }))
      }
    } catch (err: any) {
      // Silently handle - use fallback empty accounts for now
      setAccounts([])
      console.log('Accounts API not available yet')
    }
  }

  const loadSavedRecipients = async () => {
    try {
      const recipients = await transfersApi.getSavedRecipients(user!.id)
      // Ensure we always get an array
      const recipientsArray = Array.isArray(recipients) ? recipients : []
      setSavedRecipients(recipientsArray)
    } catch (err: any) {
      // Silently handle - feature not yet implemented
      setSavedRecipients([])
      console.log('Recipients feature not available yet')
    }
  }

  const loadSupportedBanks = async () => {
    try {
      const bankList = await transfersApi.getSupportedBanks()
      // Ensure we always get an array
      const banksArray = Array.isArray(bankList) ? bankList : []
      setBanks(banksArray)
    } catch (err: any) {
      // Silently handle - feature not yet implemented
      setBanks([])
      console.log('Banks feature not available yet')
    }
  }

  const loadTransferHistory = async () => {
    try {
      setLoading(true)
      const response = await transfersApi.getUserTransfers(user!.id, 0, 50)
      // Ensure we always get an array
      const transfersArray = Array.isArray(response?.content) ? response.content : []
      setTransfers(transfersArray)
    } catch (err: any) {
      setError(err.message || 'Failed to load transfer history')
      setTransfers([])
    } finally {
      setLoading(false)
    }
  }

  const calculateFees = async () => {
    if (transferForm.amount <= 0 || !transferForm.fromAccountId) return

    try {
      const feeRequest = {
        fromAccountId: transferForm.fromAccountId,
        amount: transferForm.amount,
        transferType: transferForm.transferType,
        toAccountId: transferForm.toAccountId,
        toBankCode: transferForm.toBankCode,
        currency: transferForm.currency,
      }

      console.log('Calculating fees for:', feeRequest)
      const fees = await transfersApi.calculateTransferFee(feeRequest)
      console.log('Fee calculation result:', fees)
      setFeeInfo(fees)
    } catch (err: any) {
      console.error('Failed to calculate fees:', err.message || err)
      // Set null to indicate fee calculation failed but don't block the transfer
      setFeeInfo(null)
    }
  }

  // Calculate fees when relevant form fields change
  useEffect(() => {
    if (transferForm.amount > 0) {
      const timeoutId = setTimeout(calculateFees, 500)
      return () => clearTimeout(timeoutId)
    }
  }, [
    transferForm.amount,
    transferForm.transferType,
    transferForm.fromAccountId,
    transferForm.toBankCode,
  ])

  const handleRecipientSelect = (recipient: SavedRecipient) => {
    setSelectedRecipient(recipient)
    setTransferForm(prev => ({
      ...prev,
      recipientName: recipient.recipientName,
      toAccountNumber: recipient.accountNumber,
      toBankCode: recipient.bankCode,
      recipientPhone: recipient.phoneNumber,
      recipientEmail: recipient.email,
      transferType:
        recipient.recipientType === 'BANK_ACCOUNT'
          ? 'BANK_TRANSFER'
          : recipient.recipientType === 'MOBILE_MONEY'
            ? 'MOBILE_MONEY'
            : 'INTERNAL',
    }))
  }

  const handleSubmitTransfer = async (e: React.FormEvent) => {
    e.preventDefault()
    setShowConfirmation(true)
  }

  const lookupAccount = async (accountNumber: string, bankCode?: string) => {
    if (!accountNumber || accountNumber.length < 8) {
      setAccountLookup({ loading: false, result: null, error: null })
      return
    }

    try {
      setAccountLookup({ loading: true, result: null, error: null })
      
      // For internal transfers, lookup Telepesa account
      if (transferForm.transferType === 'INTERNAL') {
        const response = await transfersApi.lookupAccount(accountNumber)
        setAccountLookup({ 
          loading: false, 
          result: {
            accountName: response.accountName,
            accountType: response.accountType
          }, 
          error: null 
        })
        
        // Auto-fill recipient name if not already set
        if (!transferForm.recipientName || !selectedRecipient) {
          setTransferForm(prev => ({ ...prev, recipientName: response.accountName }))
        }
      }
      
      // For bank transfers, lookup external bank account
      else if (transferForm.transferType === 'BANK_TRANSFER' && bankCode) {
        const response = await transfersApi.lookupBankAccount(accountNumber, bankCode)
        setAccountLookup({ 
          loading: false, 
          result: {
            accountName: response.accountName,
            accountType: response.accountType || 'CHECKING',
            bankName: response.bankName
          }, 
          error: null 
        })
        
        // Auto-fill recipient name if not already set
        if (!transferForm.recipientName || !selectedRecipient) {
          setTransferForm(prev => ({ ...prev, recipientName: response.accountName }))
        }
      }
    } catch (err: any) {
      console.log('Account lookup failed:', err.message)
      setAccountLookup({ 
        loading: false, 
        result: null, 
        error: err.message || 'Account not found' 
      })
    }
  }

  // Debounced account lookup when account number changes
  useEffect(() => {
    const accountNumber = transferForm.toAccountNumber
    const bankCode = transferForm.toBankCode
    
    if (accountNumber && accountNumber.length >= 8) {
      const timeoutId = setTimeout(() => {
        lookupAccount(accountNumber, bankCode)
      }, 1000) // Wait 1 second after user stops typing
      
      return () => clearTimeout(timeoutId)
    } else {
      setAccountLookup({ loading: false, result: null, error: null })
    }
  }, [transferForm.toAccountNumber, transferForm.toBankCode, transferForm.transferType])

  const confirmTransfer = async () => {
    try {
      setLoading(true)
      const transfer = await transfersApi.createTransfer(transferForm)
      setTransferSuccess(transfer)
      setShowConfirmation(false)

      // Reset form
      setTransferForm({
        fromAccountId: Array.isArray(accounts) && accounts.length > 0 ? accounts[0].id : 0,
        recipientName: '',
        amount: 0,
        transferType: 'INTERNAL',
        description: '',
        saveRecipient: false,
      })
      setFeeInfo(null)
      setSelectedRecipient(null)

      // Reload data
      await loadUserAccounts()
      if (activeTab === 'history') {
        await loadTransferHistory()
      }
    } catch (err: any) {
      setError(err.message || 'Transfer failed')
      setShowConfirmation(false)
    } finally {
      setLoading(false)
    }
  }

  const renderNewTransferTab = () => (
    <div className="space-y-6">
      {/* Success Message */}
      {transferSuccess && (
        <div className="p-6 bg-green-50 border border-green-200 rounded-financial animate-bounce-in">
          <div className="flex items-center">
            <span className="text-3xl mr-4">🎉</span>
            <div>
              <h3 className="font-semibold text-green-800">Transfer Successful!</h3>
              <p className="text-green-700">
                {formatCurrency(transferSuccess.amount)} has been sent to{' '}
                {transferSuccess.recipientName}
              </p>
              <p className="text-sm text-green-600 mt-1">
                Transfer ID: {transferSuccess.transferId}
              </p>
            </div>
          </div>
          <Button
            variant="ghost"
            size="sm"
            onClick={() => setTransferSuccess(null)}
            className="mt-3 text-green-600"
          >
            ✕ Dismiss
          </Button>
        </div>
      )}

      {/* Transfer Form */}
      <form onSubmit={handleSubmitTransfer} className="space-y-6">
        {/* From Account Selection */}
        <Card className="p-6">
          <h3 className="text-lg font-semibold text-financial-navy mb-4">💰 From Account</h3>
          <div className="grid gap-3">
            {Array.isArray(accounts) && accounts.length > 0 ? (
              accounts.map(account => (
                <div
                  key={account.id}
                  onClick={() => setTransferForm(prev => ({ ...prev, fromAccountId: account.id }))}
                  className={cn(
                    'p-4 border rounded-financial cursor-pointer transition-all',
                    transferForm.fromAccountId === account.id
                      ? 'border-financial-blue bg-blue-50'
                      : 'border-gray-200 hover:border-gray-300'
                  )}
                >
                  <div className="flex justify-between items-center">
                    <div>
                      <div className="font-medium text-financial-navy">{account.accountName}</div>
                      <div className="text-sm text-financial-gray">
                        •••• {account.accountNumber.slice(-4)} • {account.accountType}
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
              ))
            ) : (
              <div className="p-8 text-center">
                <div className="text-4xl mb-3">💳</div>
                <h3 className="font-semibold text-financial-navy mb-2">No accounts available</h3>
                <p className="text-financial-gray text-sm">
                  You need at least one active account to make transfers.
                </p>
              </div>
            )}
          </div>
        </Card>

        {/* Transfer Type Selection */}
        <Card className="p-6">
          <h3 className="text-lg font-semibold text-financial-navy mb-4">🔄 Transfer Type</h3>
          <div className="grid grid-cols-2 md:grid-cols-4 gap-3">
            {Object.entries(TRANSFER_TYPE_ICONS).map(([type, icon]) => (
              <button
                key={type}
                type="button"
                onClick={() => setTransferForm(prev => ({ ...prev, transferType: type as any }))}
                className={cn(
                  'p-4 border rounded-financial text-center transition-all',
                  transferForm.transferType === type
                    ? 'border-financial-blue bg-blue-50'
                    : 'border-gray-200 hover:border-gray-300'
                )}
              >
                <div className="text-2xl mb-2">{icon}</div>
                <div className="text-sm font-medium">{type.replace('_', ' ')}</div>
              </button>
            ))}
          </div>
        </Card>

        {/* Saved Recipients (if any) */}
        {Array.isArray(savedRecipients) && savedRecipients.length > 0 && (
          <Card className="p-6">
            <h3 className="text-lg font-semibold text-financial-navy mb-4">
              👥 Quick Select Recipient
            </h3>
            <div className="grid gap-3">
              {savedRecipients.slice(0, 3).map(recipient => (
                <button
                  key={recipient.id}
                  type="button"
                  onClick={() => handleRecipientSelect(recipient)}
                  className={cn(
                    'p-3 border rounded-financial text-left transition-all',
                    selectedRecipient?.id === recipient.id
                      ? 'border-financial-blue bg-blue-50'
                      : 'border-gray-200 hover:border-gray-300'
                  )}
                >
                  <div className="font-medium text-financial-navy">{recipient.recipientName}</div>
                  <div className="text-sm text-financial-gray">
                    {recipient.accountNumber && `•••• ${recipient.accountNumber.slice(-4)}`}
                    {recipient.bankName && ` • ${recipient.bankName}`}
                    {recipient.phoneNumber && ` • ${recipient.phoneNumber}`}
                  </div>
                </button>
              ))}
            </div>
            {savedRecipients.length > 3 && (
              <Button
                type="button"
                variant="ghost"
                size="sm"
                onClick={() => setActiveTab('recipients')}
                className="mt-3"
              >
                View all recipients →
              </Button>
            )}
          </Card>
        )}

        {/* Recipient Details */}
        <Card className="p-6">
          <h3 className="text-lg font-semibold text-financial-navy mb-4">👤 Recipient Details</h3>
          <div className="grid md:grid-cols-2 gap-4">
            <Input
              label="Recipient Name"
              value={transferForm.recipientName}
              onChange={e => setTransferForm(prev => ({ ...prev, recipientName: e.target.value }))}
              placeholder="Enter recipient name"
              required
            />

            {transferForm.transferType === 'BANK_TRANSFER' && (
              <>
                <div>
                  <label className="block text-sm font-medium text-financial-navy mb-2">Bank</label>
                  <select
                    value={transferForm.toBankCode || ''}
                    onChange={e =>
                      setTransferForm(prev => ({ ...prev, toBankCode: e.target.value }))
                    }
                    className="w-full input"
                    required
                  >
                    <option value="">Select Bank</option>
                    {Array.isArray(banks)
                      ? banks.map(bank => (
                          <option key={bank.bankCode} value={bank.bankCode}>
                            {bank.bankName}
                          </option>
                        ))
                      : null}
                  </select>
                </div>
                <div className="relative">
                  <Input
                    label="Account Number"
                    value={transferForm.toAccountNumber || ''}
                    onChange={e => {
                      setTransferForm(prev => ({ ...prev, toAccountNumber: e.target.value }))
                      // Reset recipient name if account number changed
                      if (!selectedRecipient && transferForm.toAccountNumber !== e.target.value) {
                        setTransferForm(prev => ({ ...prev, recipientName: '' }))
                      }
                    }}
                    placeholder="Enter account number"
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
                        Looking up account...
                      </div>
                    )}
                    
                    {accountLookup.result && (
                      <div className="flex items-center text-sm text-green-600 bg-green-50 p-2 rounded">
                        <svg className="mr-2 h-4 w-4" fill="currentColor" viewBox="0 0 20 20">
                          <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clipRule="evenodd"></path>
                        </svg>
                        <div>
                          <div className="font-medium">{accountLookup.result.accountName}</div>
                          <div className="text-xs">{accountLookup.result.accountType} {accountLookup.result.bankName && `• ${accountLookup.result.bankName}`}</div>
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
              </>
            )}

            {transferForm.transferType === 'MOBILE_MONEY' && (
              <Input
                label="Phone Number"
                value={transferForm.recipientPhone || ''}
                onChange={e =>
                  setTransferForm(prev => ({ ...prev, recipientPhone: e.target.value }))
                }
                placeholder="e.g., +254712345678"
                required
              />
            )}

            {transferForm.transferType === 'INTERNAL' && (
              <div className="relative">
                <Input
                  label="Account Number"
                  value={transferForm.toAccountNumber || ''}
                  onChange={e => {
                    setTransferForm(prev => ({ ...prev, toAccountNumber: e.target.value }))
                    // Reset recipient name if account number changed
                    if (!selectedRecipient && transferForm.toAccountNumber !== e.target.value) {
                      setTransferForm(prev => ({ ...prev, recipientName: '' }))
                    }
                  }}
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
            )}
          </div>
        </Card>

        {/* Amount and Description */}
        <Card className="p-6">
          <h3 className="text-lg font-semibold text-financial-navy mb-4">💸 Transfer Details</h3>
          <div className="grid md:grid-cols-2 gap-4">
            <Input
              label="Amount"
              type="number"
              min="1"
              step="0.01"
              value={transferForm.amount}
              onChange={e => setTransferForm(prev => ({ ...prev, amount: Number(e.target.value) }))}
              placeholder="0.00"
              required
            />
            <Input
              label="Description"
              value={transferForm.description}
              onChange={e => setTransferForm(prev => ({ ...prev, description: e.target.value }))}
              placeholder="What's this for?"
              required
            />
          </div>

          {/* Fee Information */}
          {feeInfo && (
            <div className="mt-4 p-4 bg-blue-50 border border-blue-200 rounded-financial">
              <h4 className="font-medium text-financial-navy mb-2">💰 Fee Breakdown</h4>
              <div className="space-y-1 text-sm">
                <div className="flex justify-between">
                  <span>Amount:</span>
                  <span>{formatCurrency(transferForm.amount)}</span>
                </div>
                <div className="flex justify-between">
                  <span>Transfer Fee:</span>
                  <span>{formatCurrency(feeInfo.fee)}</span>
                </div>
                <div className="flex justify-between font-semibold border-t border-blue-200 pt-1">
                  <span>Total:</span>
                  <span>{formatCurrency(feeInfo.totalAmount)}</span>
                </div>
                {feeInfo.estimatedArrival && (
                  <div className="text-financial-gray mt-2">
                    <span>⏰ Estimated arrival: {feeInfo.estimatedArrival}</span>
                  </div>
                )}
              </div>
            </div>
          )}

          {/* Save Recipient Option */}
          <div className="mt-4">
            <label className="flex items-center">
              <input
                type="checkbox"
                checked={transferForm.saveRecipient || false}
                onChange={e =>
                  setTransferForm(prev => ({ ...prev, saveRecipient: e.target.checked }))
                }
                className="mr-2"
              />
              <span className="text-sm text-financial-navy">
                💾 Save this recipient for future transfers
              </span>
            </label>
          </div>
        </Card>

        {/* Submit Button */}
        <Button
          type="submit"
          disabled={loading || !transferForm.recipientName || transferForm.amount <= 0}
          className="w-full"
          size="lg"
        >
          {loading ? '⏳ Processing...' : '🚀 Review Transfer'}
        </Button>
      </form>

      {/* Confirmation Modal */}
      {showConfirmation && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
          <div className="bg-white rounded-financial-lg max-w-md w-full p-6 animate-bounce-in">
            <h2 className="text-xl font-bold text-financial-navy mb-4">🔍 Confirm Transfer</h2>

            <div className="space-y-4">
              <div className="p-4 bg-gray-50 rounded-financial">
                <div className="space-y-2 text-sm">
                  <div className="flex justify-between">
                    <span className="text-financial-gray">To:</span>
                    <span className="font-medium">{transferForm.recipientName}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-financial-gray">Amount:</span>
                    <span className="font-medium">{formatCurrency(transferForm.amount)}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-financial-gray">Fee:</span>
                    <span>{feeInfo ? formatCurrency(feeInfo.fee) : 'Calculating...'}</span>
                  </div>
                  <div className="flex justify-between font-semibold text-financial-navy border-t border-gray-200 pt-2">
                    <span>Total:</span>
                    <span>{feeInfo ? formatCurrency(feeInfo.totalAmount) : formatCurrency(transferForm.amount)}</span>
                  </div>
                  {!feeInfo && (
                    <div className="text-xs text-yellow-600 bg-yellow-50 p-2 rounded mt-2">
                      ⚠️ Fee calculation unavailable. Transfer may include additional charges.
                    </div>
                  )}
                </div>
              </div>

              <div className="flex gap-3">
                <Button
                  type="button"
                  variant="secondary"
                  onClick={() => setShowConfirmation(false)}
                  className="flex-1"
                >
                  Cancel
                </Button>
                <Button onClick={confirmTransfer} disabled={loading} className="flex-1">
                  {loading ? '⏳ Sending...' : '✅ Confirm'}
                </Button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  )

  const renderHistoryTab = () => (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <h2 className="text-xl font-semibold text-financial-navy">📊 Transfer History</h2>
        <Button onClick={loadTransferHistory} disabled={loading} size="sm">
          🔄 Refresh
        </Button>
      </div>

      {(!Array.isArray(transfers) || transfers.length === 0) && !loading ? (
        <div className="text-center py-12">
          <div className="text-6xl mb-4">💸</div>
          <h3 className="text-xl font-semibold text-financial-navy mb-2">No transfers yet</h3>
          <p className="text-financial-gray mb-6">
            Your transfer history will appear here once you make your first transfer.
          </p>
          <Button onClick={() => setActiveTab('new-transfer')}>🚀 Make Your First Transfer</Button>
        </div>
      ) : (
        <div className="space-y-4">
          {Array.isArray(transfers)
            ? transfers.map(transfer => (
                <Card key={transfer.id} className="p-6">
                  <div className="flex items-center justify-between">
                    <div className="flex items-center space-x-4">
                      <div className="text-3xl">{TRANSFER_TYPE_ICONS[transfer.transferType]}</div>
                      <div>
                        <div className="font-semibold text-financial-navy">
                          To: {transfer.recipientName}
                        </div>
                        <div className="text-sm text-financial-gray">
                          {transfer.description} •{' '}
                          {new Date(transfer.createdAt).toLocaleDateString()}
                        </div>
                        <div className="text-xs text-financial-gray">ID: {transfer.transferId}</div>
                      </div>
                    </div>
                    <div className="text-right">
                      <div className="font-semibold text-financial-navy">
                        {formatCurrency(transfer.amount)}
                      </div>
                      <div
                        className={cn(
                          'inline-block px-2 py-1 rounded text-xs font-medium border',
                          TRANSFER_STATUS_COLORS[transfer.status]
                        )}
                      >
                        {transfer.status}
                      </div>
                      {transfer.fee > 0 && (
                        <div className="text-xs text-financial-gray mt-1">
                          Fee: {formatCurrency(transfer.fee)}
                        </div>
                      )}
                    </div>
                  </div>

                  {(transfer.status === 'PENDING' || transfer.status === 'FAILED') && (
                    <div className="flex gap-2 mt-4 pt-4 border-t border-gray-100">
                      {transfer.status === 'PENDING' && (
                        <Button
                          size="sm"
                          variant="outline"
                          onClick={async () => {
                            try {
                              await transfersApi.cancelTransfer(transfer.id)
                              loadTransferHistory()
                            } catch (_err) {
                               
                              setError('Failed to cancel transfer')
                            }
                          }}
                        >
                          ✕ Cancel
                        </Button>
                      )}
                      {transfer.status === 'FAILED' && (
                        <Button
                          size="sm"
                          onClick={async () => {
                            try {
                              await transfersApi.retryTransfer(transfer.id)
                              loadTransferHistory()
                            } catch (_err) {
                               
                              setError('Failed to retry transfer')
                            }
                          }}
                        >
                          🔄 Retry
                        </Button>
                      )}
                    </div>
                  )}
                </Card>
              ))
            : null}
        </div>
      )}
    </div>
  )

  const renderRecipientsTab = () => (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <h2 className="text-xl font-semibold text-financial-navy">👥 Saved Recipients</h2>
        <Button onClick={loadSavedRecipients} disabled={loading} size="sm">
          🔄 Refresh
        </Button>
      </div>

      {!Array.isArray(savedRecipients) || savedRecipients.length === 0 ? (
        <div className="text-center py-12">
          <div className="text-6xl mb-4">👥</div>
          <h3 className="text-xl font-semibold text-financial-navy mb-2">No saved recipients</h3>
          <p className="text-financial-gray mb-6">
            Save recipients when making transfers for quick access next time.
          </p>
          <Button onClick={() => setActiveTab('new-transfer')}>🚀 Make a Transfer</Button>
        </div>
      ) : (
        <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-4">
          {Array.isArray(savedRecipients)
            ? savedRecipients.map(recipient => (
                <Card key={recipient.id} className="p-4">
                  <div className="flex justify-between items-start mb-3">
                    <div className="flex-1">
                      <div className="font-semibold text-financial-navy">
                        {recipient.recipientName}
                      </div>
                      {recipient.nickname && (
                        <div className="text-sm text-financial-gray">"{recipient.nickname}"</div>
                      )}
                    </div>
                    <div className="text-2xl">
                      {recipient.recipientType === 'BANK_ACCOUNT'
                        ? '🏛️'
                        : recipient.recipientType === 'MOBILE_MONEY'
                          ? '📱'
                          : '🏦'}
                    </div>
                  </div>

                  <div className="space-y-1 text-sm text-financial-gray mb-4">
                    {recipient.accountNumber && <div>•••• {recipient.accountNumber.slice(-4)}</div>}
                    {recipient.bankName && <div>{recipient.bankName}</div>}
                    {recipient.phoneNumber && <div>{recipient.phoneNumber}</div>}
                  </div>

                  <div className="flex gap-2">
                    <Button
                      size="sm"
                      onClick={() => {
                        handleRecipientSelect(recipient)
                        setActiveTab('new-transfer')
                      }}
                      className="flex-1"
                    >
                      💸 Transfer
                    </Button>
                    <Button
                      size="sm"
                      variant="outline"
                      onClick={async () => {
                        try {
                          await transfersApi.deleteSavedRecipient(recipient.id)
                          loadSavedRecipients()
                        } catch (_err) {
                           
                          setError('Failed to delete recipient')
                        }
                      }}
                    >
                      🗑️
                    </Button>
                  </div>
                </Card>
              ))
            : null}
        </div>
      )}
    </div>
  )

  return (
    <div className="max-w-7xl mx-auto p-6 space-y-8">
      {/* Header */}
      <div>
        <h1 className="text-3xl font-bold text-financial-navy mb-2">Money Transfer 💸</h1>
        <p className="text-financial-gray">
          Send money securely to bank accounts, mobile wallets, and other Telepesa users
        </p>
      </div>

      {/* Error Display */}
      {error && (
        <div className="p-4 bg-red-50 border border-red-200 rounded-financial">
          <p className="text-red-600 flex items-center">
            <span className="mr-2">⚠️</span>
            {error}
          </p>
          <Button
            variant="ghost"
            size="sm"
            onClick={() => setError(null)}
            className="mt-2 text-red-600"
          >
            Dismiss
          </Button>
        </div>
      )}

      {/* Tabs */}
      <div className="border-b border-gray-200">
        <nav className="-mb-px flex space-x-8">
          {[
            { id: 'new-transfer', label: '🚀 New Transfer', icon: '🚀' },
            { id: 'history', label: '📊 History', icon: '📊' },
            { id: 'recipients', label: '👥 Recipients', icon: '👥' },
          ].map(tab => (
            <button
              key={tab.id}
              onClick={() => setActiveTab(tab.id as TabType)}
              className={cn(
                'py-4 px-1 border-b-2 font-medium text-sm transition-colors duration-200',
                activeTab === tab.id
                  ? 'border-financial-blue text-financial-blue'
                  : 'border-transparent text-financial-gray hover:text-financial-navy hover:border-gray-300'
              )}
            >
              <span className="mr-2">{tab.icon}</span>
              {tab.label}
            </button>
          ))}
        </nav>
      </div>

      {/* Tab Content */}
      {activeTab === 'new-transfer' && renderNewTransferTab()}
      {activeTab === 'history' && renderHistoryTab()}
      {activeTab === 'recipients' && renderRecipientsTab()}
    </div>
  )
}
