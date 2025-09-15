import { useState, useEffect } from 'react'
import { useAuth } from '../store/auth'
import {
  transactionsApi,
  type Transaction,
} from '../api/transactions'
import { accountsApi, type Account } from '../api/accounts'
import { Card } from '../components/ui/Card'
import { Button } from '../components/ui/Button'
import { Input } from '../components/ui/Input'
import { formatCurrency, formatDate, cn } from '../lib/utils'

const TRANSACTION_TYPE_ICONS = {
  DEPOSIT: '📥',
  WITHDRAWAL: '📤',
  TRANSFER: '🔄',
  PAYMENT: '💳',
  LOAN_DISBURSEMENT: '💰',
  LOAN_REPAYMENT: '📈',
}

const TRANSACTION_TYPE_COLORS = {
  DEPOSIT: 'text-green-600',
  WITHDRAWAL: 'text-red-600',
  TRANSFER: 'text-blue-600',
  PAYMENT: 'text-purple-600',
  LOAN_DISBURSEMENT: 'text-orange-600',
  LOAN_REPAYMENT: 'text-emerald-600',
}

const STATUS_COLORS = {
  PENDING: 'text-yellow-600 bg-yellow-50 border-yellow-200',
  PROCESSING: 'text-blue-600 bg-blue-50 border-blue-200',
  COMPLETED: 'text-green-600 bg-green-50 border-green-200',
  FAILED: 'text-red-600 bg-red-50 border-red-200',
  CANCELLED: 'text-gray-600 bg-gray-50 border-gray-200',
}

export function TransactionsPage() {
  const { user } = useAuth()
  const [transactions, setTransactions] = useState<Transaction[]>([])
  const [accounts, setAccounts] = useState<Account[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [selectedTransaction, setSelectedTransaction] = useState<Transaction | null>(null)

  // Pagination
  const [currentPage, setCurrentPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [totalElements, setTotalElements] = useState(0)

  // Filters
  const [filters, setFilters] = useState({
    accountId: '',
    status: '',
    transactionType: '',
    startDate: '',
    endDate: '',
    search: '',
  })


  // Load data on mount
  useEffect(() => {
    if (user?.id) {
      loadAccounts()
      loadTransactions()
    }
  }, [user?.id, currentPage, filters])

  // Parse URL parameters for account filtering
  useEffect(() => {
    const urlParams = new URLSearchParams(window.location.search)
    const accountParam = urlParams.get('account')
    if (accountParam) {
      setFilters(prev => ({ ...prev, accountId: accountParam }))
    }
  }, [])

  const loadAccounts = async () => {
    try {
      const response = await accountsApi.getUserAccounts(user!.id, 0, 50)
      // getUserAccounts returns Account[] directly, not a paginated response
      const accounts = Array.isArray(response) ? response : []
      setAccounts(accounts.filter(acc => acc.status === 'ACTIVE'))
      console.log('[TransactionsPage] Loaded accounts:', accounts.length, accounts)
    } catch (err) {
      console.error('Failed to load accounts:', err)
      setAccounts([]) // Ensure we set an empty array on error
    }
  }

  const loadTransactions = async () => {
    try {
      setLoading(true)

      let response
      if (filters.accountId) {
        // Load transactions for specific account
        response = await transactionsApi.getAccountTransactions(
          Number(filters.accountId),
          currentPage,
          20
        )
      } else if (filters.search) {
        // Search transactions
        response = await transactionsApi.searchTransactions(filters.search, currentPage, 20)
      } else {
        // Load user transactions with filters
        response = await transactionsApi.getUserTransactions(
          user!.id,
          currentPage,
          20,
          filters.status || undefined,
          filters.transactionType || undefined,
          filters.startDate || undefined,
          filters.endDate || undefined
        )
      }

      setTransactions(response.content)
      setTotalPages(response.totalPages)
      setTotalElements(response.totalElements)
    } catch (err: any) {
      setError(err.message || 'Failed to load transactions')
    } finally {
      setLoading(false)
    }
  }


  const handleFilterChange = (newFilters: typeof filters) => {
    setFilters(newFilters)
    setCurrentPage(0) // Reset to first page when filters change
  }

  const clearFilters = () => {
    setFilters({
      accountId: '',
      status: '',
      transactionType: '',
      startDate: '',
      endDate: '',
      search: '',
    })
    setCurrentPage(0)
  }

  const getTransactionAmountDisplay = (transaction: Transaction) => {
    const isIncoming = ['DEPOSIT', 'LOAN_DISBURSEMENT'].includes(transaction.transactionType)
    const prefix = isIncoming ? '+' : '-'
    const colorClass = isIncoming ? 'text-green-600' : 'text-red-600'

    return (
      <span className={cn('font-semibold', colorClass)}>
        {prefix}
        {formatCurrency(transaction.amount)}
      </span>
    )
  }

  if (loading && transactions.length === 0) {
    return (
      <div className="max-w-7xl mx-auto p-6">
        <div className="animate-pulse space-y-6">
          <div className="h-8 bg-gray-200 rounded w-1/4"></div>
          <div className="space-y-4">
            {[...Array(8)].map((_, i) => (
              <div key={i} className="h-20 bg-gray-200 rounded-financial"></div>
            ))}
          </div>
        </div>
      </div>
    )
  }

  return (
    <div className="max-w-7xl mx-auto p-6 space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div>
          <h1 className="text-3xl font-bold text-financial-navy mb-2">Transaction History 💳</h1>
          <p className="text-financial-gray">
            Track all your financial activities and transactions
          </p>
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

      {/* Filters */}
      <Card className="p-6">
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
          <div>
            <label className="block text-sm font-medium text-financial-navy mb-2">Account</label>
            <select
              value={filters.accountId}
              onChange={e => handleFilterChange({ ...filters, accountId: e.target.value })}
              className="w-full input"
            >
              <option value="">All Accounts</option>
              {accounts.map(account => (
                <option key={account.id} value={account.id}>
                  {account.accountName} (••••{account.accountNumber.slice(-4)})
                </option>
              ))}
            </select>
          </div>

          <div>
            <label className="block text-sm font-medium text-financial-navy mb-2">Status</label>
            <select
              value={filters.status}
              onChange={e => handleFilterChange({ ...filters, status: e.target.value })}
              className="w-full input"
            >
              <option value="">All Statuses</option>
              <option value="PENDING">Pending</option>
              <option value="PROCESSING">Processing</option>
              <option value="COMPLETED">Completed</option>
              <option value="FAILED">Failed</option>
              <option value="CANCELLED">Cancelled</option>
            </select>
          </div>

          <div>
            <label className="block text-sm font-medium text-financial-navy mb-2">Type</label>
            <select
              value={filters.transactionType}
              onChange={e => handleFilterChange({ ...filters, transactionType: e.target.value })}
              className="w-full input"
            >
              <option value="">All Types</option>
              <option value="DEPOSIT">Deposit</option>
              <option value="WITHDRAWAL">Withdrawal</option>
              <option value="TRANSFER">Transfer</option>
              <option value="PAYMENT">Payment</option>
              <option value="LOAN_DISBURSEMENT">Loan Disbursement</option>
              <option value="LOAN_REPAYMENT">Loan Repayment</option>
            </select>
          </div>

          <div>
            <label className="block text-sm font-medium text-financial-navy mb-2">Search</label>
            <Input
              placeholder="Search transactions..."
              value={filters.search}
              onChange={e => handleFilterChange({ ...filters, search: e.target.value })}
            />
          </div>
        </div>

        <div className="flex justify-between items-center mt-4 pt-4 border-t border-gray-100">
          <div className="text-sm text-financial-gray">
            Showing {transactions.length} of {totalElements} transactions
          </div>
          <Button variant="ghost" size="sm" onClick={clearFilters}>
            🧹 Clear Filters
          </Button>
        </div>
      </Card>


      {/* Transaction List */}
      {transactions.length === 0 && !loading ? (
        <div className="text-center py-12">
          <div className="text-6xl mb-4">💳</div>
          <h3 className="text-xl font-semibold text-financial-navy mb-2">No transactions found</h3>
          <p className="text-financial-gray mb-6">
            {Object.values(filters).some(v => v)
              ? 'Try adjusting your filters.'
              : 'No transactions available yet.'}
          </p>
        </div>
      ) : (
        <div className="space-y-4">
          {transactions.map(transaction => (
            <Card
              key={transaction.id}
              className="hover-lift cursor-pointer p-6"
              onClick={() =>
                setSelectedTransaction(
                  selectedTransaction?.id === transaction.id ? null : transaction
                )
              }
            >
              <div className="flex items-center justify-between">
                <div className="flex items-center space-x-4">
                  <div className="text-2xl">
                    {TRANSACTION_TYPE_ICONS[transaction.transactionType]}
                  </div>

                  <div>
                    <div className="flex items-center gap-2">
                      <h3 className="font-semibold text-financial-navy">
                        {transaction.description}
                      </h3>
                      <div
                        className={cn(
                          'px-2 py-1 rounded text-xs font-medium border',
                          STATUS_COLORS[transaction.status]
                        )}
                      >
                        {transaction.status}
                      </div>
                    </div>

                    <div className="flex items-center gap-4 text-sm text-financial-gray mt-1">
                      <span className={TRANSACTION_TYPE_COLORS[transaction.transactionType]}>
                        {transaction.transactionType}
                      </span>
                      <span>•</span>
                      <span>{formatDate(transaction.createdAt)}</span>
                      <span>•</span>
                      <span>ID: {transaction.transactionId}</span>
                    </div>
                  </div>
                </div>

                <div className="text-right">
                  <div className="text-lg font-semibold">
                    {getTransactionAmountDisplay(transaction)}
                  </div>
                  <div className="text-sm text-financial-gray">ID: {transaction.transactionId}</div>
                </div>
              </div>

              {/* Expanded Details */}
              {selectedTransaction?.id === transaction.id && (
                <div className="mt-6 pt-4 border-t border-gray-100 animate-slide-up">
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                    <div className="space-y-3">
                      <h4 className="font-medium text-financial-navy">Transaction Details</h4>
                      <div className="space-y-2 text-sm">
                        <div className="flex justify-between">
                          <span className="text-financial-gray">Transaction ID:</span>
                          <span className="font-mono">{transaction.transactionId}</span>
                        </div>
                        <div className="flex justify-between">
                          <span className="text-financial-gray">From Account:</span>
                          <span className="font-mono">{transaction.fromAccountId || 'N/A'}</span>
                        </div>
                        <div className="flex justify-between">
                          <span className="text-financial-gray">To Account:</span>
                          <span>{transaction.toAccountId || 'N/A'}</span>
                        </div>
                        {transaction.referenceNumber && (
                          <div className="flex justify-between">
                            <span className="text-financial-gray">Reference:</span>
                            <span className="font-mono">{transaction.referenceNumber}</span>
                          </div>
                        )}
                        {transaction.balanceAfter !== undefined && (
                          <div className="flex justify-between">
                            <span className="text-financial-gray">Balance After:</span>
                            <span className="font-semibold">
                              {formatCurrency(transaction.balanceAfter)}
                            </span>
                          </div>
                        )}
                      </div>
                    </div>

                    <div className="space-y-3">
                      <h4 className="font-medium text-financial-navy">Timestamps</h4>
                      <div className="space-y-2 text-sm">
                        <div className="flex justify-between">
                          <span className="text-financial-gray">Created:</span>
                          <span>{formatDate(transaction.createdAt)}</span>
                        </div>
                        <div className="flex justify-between">
                          <span className="text-financial-gray">Updated:</span>
                          <span>{formatDate(transaction.updatedAt)}</span>
                        </div>
                        {transaction.processedAt && (
                          <div className="flex justify-between">
                            <span className="text-financial-gray">Processed:</span>
                            <span>{formatDate(transaction.processedAt)}</span>
                          </div>
                        )}
                      </div>
                    </div>
                  </div>

                  {transaction.status === 'PENDING' && (
                    <div className="flex gap-2 pt-4 border-t border-gray-100 mt-4">
                      <Button
                        size="sm"
                        variant="outline"
                        onClick={(e?: React.MouseEvent<HTMLButtonElement>) => {
                          e?.stopPropagation()
                          // Add cancel transaction functionality
                          // TODO: Implement cancel transaction API call
                        }}
                      >
                        ❌ Cancel
                      </Button>
                    </div>
                  )}
                </div>
              )}
            </Card>
          ))}
        </div>
      )}

      {/* Pagination */}
      {totalPages > 1 && (
        <div className="flex justify-center items-center space-x-4">
          <Button
            variant="outline"
            disabled={currentPage === 0}
            onClick={() => setCurrentPage(currentPage - 1)}
          >
            ← Previous
          </Button>

          <span className="text-sm text-financial-gray">
            Page {currentPage + 1} of {totalPages}
          </span>

          <Button
            variant="outline"
            disabled={currentPage >= totalPages - 1}
            onClick={() => setCurrentPage(currentPage + 1)}
          >
            Next →
          </Button>
        </div>
      )}
    </div>
  )
}
