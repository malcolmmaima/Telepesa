import React, { useState } from 'react'
import { Modal } from './ui/Modal'
import { Button } from './ui/Button'
import { Input } from './ui/Input'
import { Card } from './ui/Card'
import { formatCurrency, cn } from '../lib/utils'
import { Account } from '../api/accounts'

const ACCOUNT_TYPE_ICONS = {
  SAVINGS: '🏦',
  CHECKING: '💳',
  FIXED_DEPOSIT: '💰',
  BUSINESS: '🏢',
}

const ACCOUNT_TYPE_COLORS = {
  SAVINGS: 'from-blue-500 to-blue-600',
  CHECKING: 'from-green-500 to-green-600',
  FIXED_DEPOSIT: 'from-purple-500 to-purple-600',
  BUSINESS: 'from-orange-500 to-orange-600',
}

const STATUS_COLORS = {
  ACTIVE: 'text-green-600 bg-green-50 border-green-200',
  PENDING: 'text-yellow-600 bg-yellow-50 border-yellow-200',
  FROZEN: 'text-blue-600 bg-blue-50 border-blue-200',
  CLOSED: 'text-red-600 bg-red-50 border-red-200',
}

interface AccountDetailsModalProps {
  isOpen: boolean
  onClose: () => void
  account: Account | null
  onAccountAction: (accountId: number, action: 'activate' | 'freeze' | 'unfreeze') => Promise<void>
  loading?: boolean
}

type ActionTab = 'details' | 'transfer' | 'deposit' | 'withdraw'

export function AccountDetailsModal({ 
  isOpen, 
  onClose, 
  account, 
  onAccountAction, 
  loading = false 
}: AccountDetailsModalProps) {
  const [activeTab, setActiveTab] = useState<ActionTab>('details')
  const [actionLoading, setActionLoading] = useState(false)
  const [transferAmount, setTransferAmount] = useState('')
  const [depositAmount, setDepositAmount] = useState('')
  const [withdrawAmount, setWithdrawAmount] = useState('')
  const [recipientAccount, setRecipientAccount] = useState('')

  if (!account) return null

  const handleAccountAction = async (action: 'activate' | 'freeze' | 'unfreeze') => {
    try {
      setActionLoading(true)
      await onAccountAction(account.id, action)
      // Don't close modal - let parent handle the success state
    } catch (error) {
      console.error(`Failed to ${action} account:`, error)
    } finally {
      setActionLoading(false)
    }
  }

  const handleTransfer = () => {
    // Navigate to transfer page with pre-filled data
    const params = new URLSearchParams({
      fromAccount: account.id.toString(),
      amount: transferAmount,
      toAccount: recipientAccount
    })
    window.location.href = `/transfer?${params.toString()}`
  }

  const handleDeposit = () => {
    // Navigate to deposits/transactions page
    window.location.href = `/transactions?account=${account.id}&type=deposit&amount=${depositAmount}`
  }

  const handleWithdraw = () => {
    // Navigate to transactions page for withdrawal
    window.location.href = `/transactions?account=${account.id}&type=withdrawal&amount=${withdrawAmount}`
  }

  const tabs = [
    { id: 'details' as ActionTab, label: '📋 Details', icon: '📋' },
    { id: 'transfer' as ActionTab, label: '💸 Transfer', icon: '💸' },
    { id: 'deposit' as ActionTab, label: '💰 Deposit', icon: '💰' },
    { id: 'withdraw' as ActionTab, label: '🏧 Withdraw', icon: '🏧' },
  ]

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      size="lg"
      title={
        <div className="flex items-center gap-3">
          <span className="text-2xl">{ACCOUNT_TYPE_ICONS[account.accountType]}</span>
          <div>
            <div className="font-semibold text-financial-navy">{account.accountName}</div>
            <div className="text-sm text-financial-gray">
              •••• {account.accountNumber.slice(-4)}
            </div>
          </div>
        </div>
      }
    >
      <div className="space-y-6">
        {/* Account Header Card */}
        <Card className="overflow-hidden">
          <div
            className={cn(
              'h-20 bg-gradient-to-r relative',
              ACCOUNT_TYPE_COLORS[account.accountType]
            )}
          >
            <div className="absolute inset-0 bg-black bg-opacity-10"></div>
            <div className="relative p-4 text-white flex justify-between items-center">
              <div>
                <div className="text-sm opacity-90">Current Balance</div>
                <div className="text-2xl font-bold">{formatCurrency(account.balance)}</div>
              </div>
              <div
                className={cn(
                  'px-3 py-1 rounded-full text-xs font-medium border',
                  STATUS_COLORS[account.status]
                )}
              >
                {account.status}
              </div>
            </div>
          </div>
        </Card>

        {/* Tab Navigation */}
        <div className="border-b border-gray-200">
          <nav className="flex space-x-1">
            {tabs.map((tab) => (
              <button
                key={tab.id}
                onClick={() => setActiveTab(tab.id)}
                className={cn(
                  'px-4 py-2 text-sm font-medium rounded-t-lg transition-colors',
                  activeTab === tab.id
                    ? 'bg-financial-blue text-white border-b-2 border-financial-blue'
                    : 'text-financial-gray hover:text-financial-navy hover:bg-gray-50'
                )}
              >
                {tab.label}
              </button>
            ))}
          </nav>
        </div>

        {/* Tab Content */}
        <div className="min-h-[300px]">
          {activeTab === 'details' && (
            <div className="space-y-6">
              {/* Account Information */}
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div className="space-y-3">
                  <div>
                    <label className="text-sm font-medium text-financial-gray">Account Number</label>
                    <div className="text-financial-navy font-mono">{account.accountNumber}</div>
                  </div>
                  <div>
                    <label className="text-sm font-medium text-financial-gray">Account Type</label>
                    <div className="text-financial-navy">
                      {account.accountType.replace('_', ' ')}
                    </div>
                  </div>
                  <div>
                    <label className="text-sm font-medium text-financial-gray">Status</label>
                    <div className={cn('inline-flex px-2 py-1 rounded text-xs font-medium', STATUS_COLORS[account.status])}>
                      {account.status}
                    </div>
                  </div>
                </div>
                <div className="space-y-3">
                  <div>
                    <label className="text-sm font-medium text-financial-gray">Created Date</label>
                    <div className="text-financial-navy">
                      {new Date(account.createdAt).toLocaleDateString()}
                    </div>
                  </div>
                  {account.minimumBalance > 0 && (
                    <div>
                      <label className="text-sm font-medium text-financial-gray">Minimum Balance</label>
                      <div className="text-financial-navy">
                        {formatCurrency(account.minimumBalance)}
                      </div>
                    </div>
                  )}
                  {account.lastTransactionAt && (
                    <div>
                      <label className="text-sm font-medium text-financial-gray">Last Transaction</label>
                      <div className="text-financial-navy">
                        {new Date(account.lastTransactionAt).toLocaleDateString()}
                      </div>
                    </div>
                  )}
                </div>
              </div>

              {/* Account Actions */}
              <div className="pt-4 border-t border-gray-100">
                <h4 className="font-medium text-financial-navy mb-3">Account Actions</h4>
                <div className="flex gap-3 flex-wrap">
                  {account.status === 'PENDING' && (
                    <Button
                      onClick={() => handleAccountAction('activate')}
                      disabled={actionLoading || loading}
                      className="flex items-center gap-2"
                    >
                      ✅ Activate Account
                    </Button>
                  )}

                  {account.status === 'ACTIVE' && (
                    <Button
                      variant="outline"
                      onClick={() => handleAccountAction('freeze')}
                      disabled={actionLoading || loading}
                      className="flex items-center gap-2"
                    >
                      🧊 Freeze Account
                    </Button>
                  )}

                  {account.status === 'FROZEN' && (
                    <Button
                      variant="outline"
                      onClick={() => handleAccountAction('unfreeze')}
                      disabled={actionLoading || loading}
                      className="flex items-center gap-2"
                    >
                      🔥 Unfreeze Account
                    </Button>
                  )}

                  <Button
                    variant="ghost"
                    onClick={() => window.location.href = `/transactions?account=${account.id}`}
                    className="flex items-center gap-2"
                  >
                    📊 View Transactions
                  </Button>
                </div>
              </div>
            </div>
          )}

          {activeTab === 'transfer' && (
            <div className="space-y-4">
              <div className="text-center mb-6">
                <h3 className="text-lg font-semibold text-financial-navy mb-2">💸 Transfer Money</h3>
                <p className="text-financial-gray">Send money from this account to another account</p>
              </div>

              <div className="space-y-4">
                <div>
                  <label className="block text-sm font-medium text-financial-gray mb-1">
                    Recipient Account Number
                  </label>
                  <Input
                    type="text"
                    placeholder="Enter recipient account number"
                    value={recipientAccount}
                    onChange={(e) => setRecipientAccount(e.target.value)}
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-financial-gray mb-1">
                    Transfer Amount
                  </label>
                  <Input
                    type="number"
                    placeholder="0.00"
                    value={transferAmount}
                    onChange={(e) => setTransferAmount(e.target.value)}
                  />
                  <div className="text-xs text-financial-gray mt-1">
                    Available: {formatCurrency(account.balance)}
                  </div>
                </div>

                <Button
                  onClick={handleTransfer}
                  disabled={!transferAmount || !recipientAccount || parseFloat(transferAmount) <= 0}
                  className="w-full"
                >
                  Continue Transfer
                </Button>
              </div>
            </div>
          )}

          {activeTab === 'deposit' && (
            <div className="space-y-4">
              <div className="text-center mb-6">
                <h3 className="text-lg font-semibold text-financial-navy mb-2">💰 Deposit Money</h3>
                <p className="text-financial-gray">Add money to this account</p>
              </div>

              <div className="space-y-4">
                <div>
                  <label className="block text-sm font-medium text-financial-gray mb-1">
                    Deposit Amount
                  </label>
                  <Input
                    type="number"
                    placeholder="0.00"
                    value={depositAmount}
                    onChange={(e) => setDepositAmount(e.target.value)}
                  />
                </div>

                <Button
                  onClick={handleDeposit}
                  disabled={!depositAmount || parseFloat(depositAmount) <= 0}
                  className="w-full"
                >
                  Proceed with Deposit
                </Button>
              </div>
            </div>
          )}

          {activeTab === 'withdraw' && (
            <div className="space-y-4">
              <div className="text-center mb-6">
                <h3 className="text-lg font-semibold text-financial-navy mb-2">🏧 Withdraw Money</h3>
                <p className="text-financial-gray">Withdraw money from this account</p>
              </div>

              <div className="space-y-4">
                <div>
                  <label className="block text-sm font-medium text-financial-gray mb-1">
                    Withdrawal Amount
                  </label>
                  <Input
                    type="number"
                    placeholder="0.00"
                    value={withdrawAmount}
                    onChange={(e) => setWithdrawAmount(e.target.value)}
                  />
                  <div className="text-xs text-financial-gray mt-1">
                    Available: {formatCurrency(account.balance)}
                  </div>
                </div>

                <Button
                  onClick={handleWithdraw}
                  disabled={
                    !withdrawAmount || 
                    parseFloat(withdrawAmount) <= 0 || 
                    parseFloat(withdrawAmount) > account.balance
                  }
                  className="w-full"
                >
                  Proceed with Withdrawal
                </Button>
              </div>
            </div>
          )}
        </div>
      </div>
    </Modal>
  )
}
