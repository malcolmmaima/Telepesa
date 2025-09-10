import { useState, useEffect } from 'react'
import { useAuth } from '../store/auth'
import { accountsApi, type Account } from '../api/accounts'
import { Card } from '../components/ui/Card'
import { Button } from '../components/ui/Button'
import { formatCurrency, cn } from '../lib/utils'

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

export function AccountsPage() {
  const { user } = useAuth()
  const [accounts, setAccounts] = useState<Account[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [selectedAccount, setSelectedAccount] = useState<Account | null>(null)
  const [totalBalance, setTotalBalance] = useState(0)

  // Load user accounts
  useEffect(() => {
    if (user?.id) {
      loadAccounts()
      loadTotalBalance()
    }
  }, [user?.id])

  const loadAccounts = async () => {
    try {
      setLoading(true)
      const response = await accountsApi.getUserAccounts(user!.id, 0, 50)
      const accountsArray = Array.isArray(response.content) ? response.content : []
      setAccounts(accountsArray)
    } catch (err: any) {
      setAccounts([]) // Ensure accounts is always an array
      setError(err.message || 'Failed to load accounts')
    } finally {
      setLoading(false)
    }
  }

  const loadTotalBalance = async () => {
    try {
      const balance = await accountsApi.getUserTotalBalance(user!.id)
      setTotalBalance(balance)
    } catch (err) {
      console.error('Failed to load total balance:', err)
    }
  }

  const handleAccountAction = async (
    accountId: number,
    action: 'activate' | 'freeze' | 'unfreeze'
  ) => {
    try {
      setLoading(true)

      switch (action) {
        case 'activate':
          await accountsApi.activateAccount(accountId)
          break
        case 'freeze':
          await accountsApi.freezeAccount(accountId)
          break
        case 'unfreeze':
          await accountsApi.unfreezeAccount(accountId)
          break
      }

      await loadAccounts()
    } catch (err: any) {
      setError(err.message || `Failed to ${action} account`)
    } finally {
      setLoading(false)
    }
  }

  if (loading && (!accounts || accounts.length === 0)) {
    return (
      <div className="max-w-7xl mx-auto p-6">
        <div className="animate-pulse space-y-6">
          <div className="h-8 bg-gray-200 rounded w-1/4"></div>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {[...Array(6)].map((_, i) => (
              <div key={i} className="h-48 bg-gray-200 rounded-financial-lg"></div>
            ))}
          </div>
        </div>
      </div>
    )
  }

  return (
    <div className="max-w-7xl mx-auto p-6 space-y-8">
      {/* Header Section */}
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div>
          <h1 className="text-3xl font-bold text-financial-navy mb-2">Your Accounts 🏦</h1>
          <p className="text-financial-gray">View and manage your existing accounts in one place</p>
          <div className="mt-4 p-4 bg-gradient-to-r from-financial-navy to-financial-blue rounded-financial text-white">
            <div className="text-sm opacity-90">Total Balance</div>
            <div className="text-2xl font-bold">{formatCurrency(totalBalance)}</div>
          </div>
          <div className="mt-3 p-3 bg-blue-50 border border-blue-200 rounded-financial">
            <p className="text-sm text-blue-700">
              💡 <strong>Need a new account?</strong> Contact our support team or visit any branch
              to open additional accounts.
            </p>
          </div>
        </div>
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

      {/* Accounts Grid */}
      {(!accounts || accounts.length === 0) && !loading ? (
        <div className="text-center py-12">
          <div className="text-6xl mb-4">🏦</div>
          <h3 className="text-xl font-semibold text-financial-navy mb-2">No accounts available</h3>
          <p className="text-financial-gray mb-6">
            Contact our support team or visit any branch to open your first account!
          </p>
          <div className="max-w-md mx-auto p-6 bg-blue-50 border border-blue-200 rounded-financial">
            <h4 className="font-semibold text-blue-800 mb-2">📞 Get Started</h4>
            <p className="text-blue-700 text-sm mb-3">
              Our team will help you choose the right account type for your needs.
            </p>
            <Button variant="outline" onClick={() => (window.location.href = '/support')}>
              💬 Contact Support
            </Button>
          </div>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {accounts &&
            accounts.map(account => (
              <Card
                key={account.id}
                className="hover-lift cursor-pointer relative overflow-hidden"
                onClick={() =>
                  setSelectedAccount(selectedAccount?.id === account.id ? null : account)
                }
              >
                {/* Account Card Header */}
                <div
                  className={cn(
                    'h-24 bg-gradient-to-r rounded-t-financial-lg relative',
                    ACCOUNT_TYPE_COLORS[account.accountType]
                  )}
                >
                  <div className="absolute inset-0 bg-black bg-opacity-10"></div>
                  <div className="relative p-4 text-white">
                    <div className="flex items-center justify-between">
                      <div>
                        <div className="text-2xl mb-1">
                          {ACCOUNT_TYPE_ICONS[account.accountType]}
                        </div>
                        <div className="text-sm opacity-90">
                          {account.accountType.replace('_', ' ')}
                        </div>
                      </div>
                      <div
                        className={cn(
                          'px-2 py-1 rounded text-xs font-medium border',
                          STATUS_COLORS[account.status]
                        )}
                      >
                        {account.status}
                      </div>
                    </div>
                  </div>
                </div>

                {/* Account Info */}
                <div className="p-6">
                  <h3 className="font-semibold text-financial-navy mb-2">{account.accountName}</h3>
                  <p className="text-sm text-financial-gray mb-4">
                    •••• {account.accountNumber.slice(-4)}
                  </p>

                  <div className="space-y-2">
                    <div className="flex justify-between items-center">
                      <span className="text-sm text-financial-gray">Balance:</span>
                      <span className="font-semibold text-financial-navy">
                        {formatCurrency(account.balance)}
                      </span>
                    </div>

                    {account.minimumBalance > 0 && (
                      <div className="flex justify-between items-center">
                        <span className="text-sm text-financial-gray">Min Balance:</span>
                        <span className="text-sm text-financial-gray">
                          {formatCurrency(account.minimumBalance)}
                        </span>
                      </div>
                    )}
                  </div>

                  {/* Expanded Details */}
                  {selectedAccount?.id === account.id && (
                    <div className="mt-6 pt-4 border-t border-gray-100 animate-slide-up">
                      <div className="space-y-3">
                        <div className="text-xs text-financial-gray space-y-1">
                          <div>Account #: {account.accountNumber}</div>
                          <div>Created: {new Date(account.createdAt).toLocaleDateString()}</div>
                          {account.lastTransactionAt && (
                            <div>
                              Last Activity:{' '}
                              {new Date(account.lastTransactionAt).toLocaleDateString()}
                            </div>
                          )}
                        </div>

                        {/* Account Actions */}
                        <div className="flex gap-2 pt-2">
                          {account.status === 'PENDING' && (
                            <Button
                              size="sm"
                              onClick={(e?: React.MouseEvent<HTMLButtonElement>) => {
                                e?.stopPropagation()
                                handleAccountAction(account.id, 'activate')
                              }}
                              disabled={loading}
                            >
                              ✅ Activate
                            </Button>
                          )}

                          {account.status === 'ACTIVE' && (
                            <Button
                              size="sm"
                              variant="outline"
                              onClick={(e?: React.MouseEvent<HTMLButtonElement>) => {
                                e?.stopPropagation()
                                handleAccountAction(account.id, 'freeze')
                              }}
                              disabled={loading}
                            >
                              🧊 Freeze
                            </Button>
                          )}

                          {account.status === 'FROZEN' && (
                            <Button
                              size="sm"
                              variant="outline"
                              onClick={(e?: React.MouseEvent<HTMLButtonElement>) => {
                                e?.stopPropagation()
                                handleAccountAction(account.id, 'unfreeze')
                              }}
                              disabled={loading}
                            >
                              🔥 Unfreeze
                            </Button>
                          )}

                          <Button
                            size="sm"
                            variant="ghost"
                            onClick={(e?: React.MouseEvent<HTMLButtonElement>) => {
                              e?.stopPropagation()
                              window.location.href = `/transactions?account=${account.id}`
                            }}
                          >
                            📊 Transactions
                          </Button>
                        </div>
                      </div>
                    </div>
                  )}
                </div>
              </Card>
            ))}
        </div>
      )}

      {/* Quick Stats */}
      {accounts.length > 0 && (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
          <Card className="p-4">
            <div className="text-sm text-financial-gray">Total Accounts</div>
            <div className="text-2xl font-bold text-financial-navy">{accounts.length}</div>
          </Card>

          <Card className="p-4">
            <div className="text-sm text-financial-gray">Active Accounts</div>
            <div className="text-2xl font-bold text-green-600">
              {accounts.filter(a => a.status === 'ACTIVE').length}
            </div>
          </Card>

          <Card className="p-4">
            <div className="text-sm text-financial-gray">Pending Accounts</div>
            <div className="text-2xl font-bold text-yellow-600">
              {accounts.filter(a => a.status === 'PENDING').length}
            </div>
          </Card>

          <Card className="p-4">
            <div className="text-sm text-financial-gray">Avg Balance</div>
            <div className="text-2xl font-bold text-financial-navy">
              {formatCurrency(accounts.length ? totalBalance / accounts.length : 0)}
            </div>
          </Card>
        </div>
      )}
    </div>
  )
}
