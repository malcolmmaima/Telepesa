import { useState, useEffect } from 'react'
import { useAuth } from '../store/auth'
import { accountsApi, type Account } from '../api/accounts'
import { transactionsApi, type Transaction } from '../api/transactions'
import { Card } from '../components/ui/Card'
import { Button } from '../components/ui/Button'
import { formatCurrency, formatDate } from '../lib/utils'

export function HomePage() {
  const { user } = useAuth()

  // State for real data from API
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [balanceSummary, setBalanceSummary] = useState({
    totalBalance: 0,
    totalAvailableBalance: 0,
    accountCount: 0,
    currencyCode: 'KES',
  })
  const [accounts, setAccounts] = useState<Account[]>([])
  const [recentTransactions, setRecentTransactions] = useState<Transaction[]>([])
  const [stats, setStats] = useState({
    activeAccounts: 0,
    activeLoans: 0,
    totalTransactions: 0,
  })

  const quickActions = [
    {
      id: 'transfer',
      label: 'Transfer',
      path: '/transfers',
      icon: '🔄',
      color: 'from-blue-500 to-blue-600',
    },
    {
      id: 'pay',
      label: 'Pay Bills',
      path: '/payments',
      icon: '💳',
      color: 'from-purple-500 to-purple-600',
    },
    {
      id: 'loan',
      label: 'Apply Loan',
      path: '/loans',
      icon: '💰',
      color: 'from-green-500 to-green-600',
    },
    {
      id: 'accounts',
      label: 'Accounts',
      path: '/accounts',
      icon: '🏦',
      color: 'from-financial-navy to-financial-blue',
    },
    {
      id: 'transactions',
      label: 'History',
      path: '/transactions',
      icon: '📊',
      color: 'from-orange-500 to-orange-600',
    },
  ]

  // Load dashboard data
  useEffect(() => {
    if (user?.id) {
      loadDashboardData()
    }
  }, [user?.id])

  const loadDashboardData = async () => {
    try {
      setLoading(true)
      setError(null)

      // Initialize with fallback values
      let accountsData: Account[] = []
      let balanceData = {
        totalBalance: 0,
        totalAvailableBalance: 0,
        accountCount: 0,
        currencyCode: 'KES',
      }
      let transactionsData: { content: Transaction[]; totalElements: number } = {
        content: [],
        totalElements: 0,
      }

      try {
        // Try to load accounts and balance
        const [accountsResponse, totalBalanceResponse] = await Promise.all([
          accountsApi.getUserAccounts(user!.id, 0, 10),
          accountsApi.getUserTotalBalance(user!.id),
        ])
        accountsData = accountsResponse
        balanceData = totalBalanceResponse
      } catch (err) {
        console.log('Accounts/balance API not available yet')
      }

      try {
        // Try to load recent transactions
        const transactionsResponse = await transactionsApi.getUserTransactions(user!.id, 0, 5)
        transactionsData = transactionsResponse
      } catch (err) {
        console.log('Transactions API not available yet')
      }

      // Ensure arrays are always defined
      const safeAccounts = Array.isArray(accountsData) ? accountsData : []
      const safeTransactions = Array.isArray(transactionsData.content)
        ? transactionsData.content
        : []

      setAccounts(safeAccounts)
      setBalanceSummary(balanceData)
      setRecentTransactions(safeTransactions)

      // Calculate stats
      setStats({
        activeAccounts: safeAccounts.filter(acc => acc.status === 'ACTIVE').length,
        activeLoans: 0, // TODO: Implement loans API
        totalTransactions: transactionsData.totalElements || 0,
      })
    } catch (err: any) {
      // Only show error for critical failures
      console.log('Some dashboard features are not available yet')
    } finally {
      setLoading(false)
    }
  }

  // Loading state
  if (loading) {
    return (
      <div className="max-w-7xl mx-auto p-6">
        <div className="animate-pulse space-y-6">
          <div className="h-8 bg-gray-200 rounded w-1/3"></div>
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
            <div className="lg:col-span-2 h-48 bg-gray-200 rounded-financial-lg"></div>
            <div className="h-48 bg-gray-200 rounded-financial-lg"></div>
          </div>
          <div className="h-64 bg-gray-200 rounded-financial-lg"></div>
        </div>
      </div>
    )
  }

  return (
    <div className="max-w-7xl mx-auto p-6 space-y-8">
      {/* Welcome Header */}
      <div>
        <h1 className="text-3xl font-bold text-financial-navy mb-2">
          Welcome back{user ? `, ${user.firstName ?? user.username}` : ''} 👋
        </h1>
        <p className="text-financial-gray">Here's what's happening with your finances today.</p>
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
            onClick={() => loadDashboardData()}
            className="mt-2 text-red-600"
          >
            🔄 Retry
          </Button>
        </div>
      )}

      {/* Financial Summary */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
        <Card className="p-6 bg-gradient-to-r from-financial-navy to-financial-blue text-white hover-lift">
          <div className="flex items-center justify-between">
            <div>
              <div className="text-sm opacity-90">Total Balance</div>
              <div className="text-2xl font-bold">
                {formatCurrency(balanceSummary.totalBalance)}
              </div>
              <div className="text-xs opacity-75">
                Available: {formatCurrency(balanceSummary.totalAvailableBalance)}
              </div>
            </div>
            <div className="text-3xl opacity-80">💰</div>
          </div>
        </Card>

        <Card className="p-6 hover-lift">
          <div className="flex items-center justify-between">
            <div>
              <div className="text-sm text-financial-gray">Active Accounts</div>
              <div className="text-2xl font-bold text-financial-navy">{stats.activeAccounts}</div>
            </div>
            <div className="text-3xl">🏦</div>
          </div>
        </Card>

        <Card className="p-6 hover-lift">
          <div className="flex items-center justify-between">
            <div>
              <div className="text-sm text-financial-gray">Transactions</div>
              <div className="text-2xl font-bold text-financial-navy">
                {stats.totalTransactions}
              </div>
            </div>
            <div className="text-3xl">📊</div>
          </div>
        </Card>

        <Card className="p-6 hover-lift">
          <div className="flex items-center justify-between">
            <div>
              <div className="text-sm text-financial-gray">Active Loans</div>
              <div className="text-2xl font-bold text-financial-navy">{stats.activeLoans}</div>
            </div>
            <div className="text-3xl">🎯</div>
          </div>
        </Card>
      </div>

      {/* Quick Actions */}
      <Card title="Quick Actions" description="Shortcuts to common tasks">
        <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-5 gap-4">
          {quickActions.map(action => (
            <div
              key={action.id}
              className={`p-4 rounded-financial-lg bg-gradient-to-r ${action.color} text-white cursor-pointer hover-lift transition-all duration-200 hover:scale-105`}
              onClick={() => (window.location.href = action.path)}
            >
              <div className="text-2xl mb-2">{action.icon}</div>
              <div className="text-sm font-medium">{action.label}</div>
            </div>
          ))}
        </div>
      </Card>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
        {/* Your Accounts */}
        <Card title="Your Accounts" description="Balances and account status" className="h-fit">
          {!accounts || accounts.length === 0 ? (
            <div className="text-center py-8">
              <div className="text-4xl mb-3">🏦</div>
              <p className="text-financial-gray mb-4">No accounts found</p>
              <Button variant="outline" onClick={() => (window.location.href = '/accounts')}>
                Create Account
              </Button>
            </div>
          ) : (
            <div className="space-y-4 max-h-80 overflow-y-auto custom-scrollbar">
              {accounts &&
                accounts.slice(0, 3).map(account => (
                  <div
                    key={account.id}
                    className="border rounded-financial p-4 bg-white hover:bg-gray-50 cursor-pointer transition-colors min-h-0 flex-shrink-0"
                    onClick={() => (window.location.href = '/accounts')}
                  >
                    <div className="flex items-center justify-between">
                      <div className="flex-1 min-w-0">
                        <div className="flex items-center gap-2 flex-wrap">
                          <div className="text-financial-navy font-medium truncate">
                            {account.accountName}
                          </div>
                          <div
                            className={`px-2 py-1 rounded text-xs font-medium flex-shrink-0 ${
                              account.status === 'ACTIVE'
                                ? 'text-green-600 bg-green-50 border border-green-200'
                                : account.status === 'PENDING'
                                  ? 'text-yellow-600 bg-yellow-50 border border-yellow-200'
                                  : 'text-gray-600 bg-gray-50 border border-gray-200'
                            }`}
                          >
                            {account.status}
                          </div>
                        </div>
                        <div className="text-sm text-financial-gray truncate">
                          •••• {account.accountNumber.slice(-4)}
                        </div>
                        <div className="text-xs text-financial-gray mt-1 truncate">
                          {account.accountType.replace('_', ' ')}
                        </div>
                      </div>
                      <div className="text-right flex-shrink-0 ml-4">
                        <div className="text-lg font-semibold text-financial-navy">
                          {formatCurrency(account.balance)}
                        </div>
                        {account.lastTransactionAt && (
                          <div className="text-xs text-financial-gray">
                            Last: {formatDate(account.lastTransactionAt, 'short')}
                          </div>
                        )}
                      </div>
                    </div>
                  </div>
                ))}
              {accounts && accounts.length > 3 && (
                <Button
                  variant="ghost"
                  size="sm"
                  onClick={() => (window.location.href = '/accounts')}
                  className="w-full flex-shrink-0"
                >
                  View All Accounts ({accounts.length})
                </Button>
              )}
            </div>
          )}
        </Card>

        {/* Recent Transactions */}
        <Card title="Recent Transactions" description="Your latest activity">
          {!recentTransactions || recentTransactions.length === 0 ? (
            <div className="text-center py-8">
              <div className="text-4xl mb-3">📊</div>
              <p className="text-financial-gray mb-4">No recent transactions</p>
              <Button variant="outline" onClick={() => (window.location.href = '/transactions')}>
                View Transactions
              </Button>
            </div>
          ) : (
            <div className="space-y-3">
              {recentTransactions &&
                recentTransactions.map(transaction => {
                  const isIncoming = ['DEPOSIT', 'LOAN_DISBURSEMENT'].includes(
                    transaction.transactionType
                  )
                  const amountColor = isIncoming ? 'text-green-600' : 'text-red-600'
                  const amountPrefix = isIncoming ? '+' : '-'

                  return (
                    <div
                      key={transaction.id}
                      className="flex items-center justify-between p-3 rounded-financial bg-gray-50 hover:bg-gray-100 cursor-pointer transition-colors"
                      onClick={() => (window.location.href = '/transactions')}
                    >
                      <div className="flex items-center space-x-3">
                        <div className="text-xl">
                          {transaction.transactionType === 'DEPOSIT' && '📥'}
                          {transaction.transactionType === 'WITHDRAWAL' && '📤'}
                          {transaction.transactionType === 'TRANSFER' && '🔄'}
                          {transaction.transactionType === 'PAYMENT' && '💳'}
                          {transaction.transactionType === 'LOAN_DISBURSEMENT' && '💰'}
                          {transaction.transactionType === 'LOAN_REPAYMENT' && '📈'}
                        </div>
                        <div>
                          <div className="font-medium text-financial-navy">
                            {transaction.description}
                          </div>
                          <div className="text-xs text-financial-gray">
                            {formatDate(transaction.createdAt, 'short')} •{' '}
                            {transaction.transactionType}
                          </div>
                        </div>
                      </div>
                      <div className={`font-semibold ${amountColor}`}>
                        {amountPrefix}
                        {formatCurrency(transaction.amount)}
                      </div>
                    </div>
                  )
                })}
              <Button
                variant="ghost"
                size="sm"
                onClick={() => (window.location.href = '/transactions')}
                className="w-full"
              >
                View All Transactions
              </Button>
            </div>
          )}
        </Card>
      </div>
    </div>
  )
}
