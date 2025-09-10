import { api } from './client'
import type { PageResponse } from './accounts'

// Transaction Types and Interfaces
export interface Transaction {
  id: number
  transactionId: string
  fromAccountId?: number
  toAccountId?: number
  userId: number
  amount: number
  transactionType: 'DEPOSIT' | 'WITHDRAWAL' | 'TRANSFER' | 'PAYMENT' | 'LOAN_DISBURSEMENT' | 'LOAN_REPAYMENT'
  status: 'PENDING' | 'PROCESSING' | 'COMPLETED' | 'FAILED' | 'CANCELLED'
  description: string
  referenceNumber?: string
  feeAmount?: number
  totalAmount: number
  balanceAfter?: number
  processedAt?: string
  createdAt: string
  updatedAt: string
}

export interface CreateTransactionRequest {
  accountId: number
  amount: number
  transactionType: Transaction['transactionType']
  description: string
  recipientAccountId?: number
  recipientAccountNumber?: string
  currencyCode?: string
}

export interface TransactionSummary {
  totalTransactions: number
  totalDeposits: number
  totalWithdrawals: number
  totalTransfers: number
  totalFees: number
  successRate: number
  avgTransactionAmount: number
}

export interface MonthlyTransactionStats {
  month: string
  deposits: number
  withdrawals: number
  transfers: number
  totalAmount: number
  transactionCount: number
}

// Transaction API Service
export const transactionsApi = {
  // Create transaction
  createTransaction: async (request: CreateTransactionRequest): Promise<Transaction> => {
    const response = await api.post('/transactions', request)
    return response.data
  },

  // Get transaction by ID
  getTransaction: async (id: number): Promise<Transaction> => {
    const response = await api.get(`/transactions/${id}`)
    return response.data
  },

  // Get transaction by transaction ID
  getTransactionByTransactionId: async (transactionId: string): Promise<Transaction> => {
    const response = await api.get(`/transactions/by-transaction-id/${transactionId}`)
    return response.data
  },

  // Get all transactions with pagination
  getTransactions: async (page = 0, size = 20): Promise<PageResponse<Transaction>> => {
    const response = await api.get('/transactions', {
      params: { page, size }
    })
    return response.data
  },

  // Get user transactions with filters
  getUserTransactions: async (
    userId: number,
    page = 0,
    size = 20,
    status?: string,
    transactionType?: string,
    startDate?: string,
    endDate?: string
  ): Promise<PageResponse<Transaction>> => {
    const params: any = { page, size }
    if (status) params.status = status
    if (transactionType) params.transactionType = transactionType
    if (startDate) params.startDate = startDate
    if (endDate) params.endDate = endDate
    
    const response = await api.get(`/transactions/user/${userId}`, {
      params
    })
    return response.data
  },

  // Search transactions
  searchTransactions: async (
    query: string,
    page = 0,
    size = 20
  ): Promise<PageResponse<Transaction>> => {
    const response = await api.get('/transactions/search', {
      params: { query, page, size }
    })
    return response.data
  },

  // Get account transactions
  getAccountTransactions: async (
    accountId: number,
    page = 0,
    size = 20
  ): Promise<PageResponse<Transaction>> => {
    const response = await api.get(`/transactions/account/${accountId}`, {
      params: { page, size }
    })
    return response.data
  },

  // Get account transaction history (all transactions for an account)
  getAccountTransactionHistory: async (accountId: number): Promise<Transaction[]> => {
    const response = await api.get(`/transactions/account/${accountId}/history`)
    return response.data
  },

  // Get transactions by status
  getTransactionsByStatus: async (
    status: Transaction['status'],
    page = 0,
    size = 20
  ): Promise<PageResponse<Transaction>> => {
    const response = await api.get(`/transactions/status/${status}`, {
      params: { page, size }
    })
    return response.data
  },

  // Get transactions by type
  getTransactionsByType: async (
    type: Transaction['transactionType'],
    page = 0,
    size = 20
  ): Promise<PageResponse<Transaction>> => {
    const response = await api.get(`/transactions/type/${type}`, {
      params: { page, size }
    })
    return response.data
  },

  // Get transactions by date range
  getTransactionsByDateRange: async (
    userId: number,
    startDate: string,
    endDate: string,
    page = 0,
    size = 20
  ): Promise<PageResponse<Transaction>> => {
    const response = await api.get(`/transactions/user/${userId}/date-range`, {
      params: { startDate, endDate, page, size }
    })
    return response.data
  },

  // Update transaction status
  updateTransactionStatus: async (
    id: number,
    status: Transaction['status']
  ): Promise<Transaction> => {
    const response = await api.put(`/transactions/${id}/status`, status, {
      headers: {
        'Content-Type': 'application/json'
      }
    })
    return response.data
  },

  // Get account balance (from transaction service perspective)
  getAccountBalance: async (accountId: number): Promise<number> => {
    const response = await api.get(`/transactions/account/${accountId}/balance`)
    return response.data
  },

  // Get total debits for an account
  getTotalDebits: async (accountId: number, since: string): Promise<number> => {
    const response = await api.get(`/transactions/account/${accountId}/debits`, {
      params: { since }
    })
    return response.data
  },

  // Get total credits for an account
  getTotalCredits: async (accountId: number, since: string): Promise<number> => {
    const response = await api.get(`/transactions/account/${accountId}/credits`, {
      params: { since }
    })
    return response.data
  },

  // Get transaction count by user and status
  getTransactionCount: async (
    userId: number,
    status: Transaction['status']
  ): Promise<number> => {
    const response = await api.get(`/transactions/user/${userId}/count`, {
      params: { status }
    })
    return response.data
  },

  // Analytics and reporting functions
  getTransactionSummary: async (
    userId: number,
    startDate?: string,
    endDate?: string
  ): Promise<TransactionSummary> => {
    // This would be a custom endpoint or computed on the frontend
    const transactions = await transactionsApi.getUserTransactions(userId, 0, 1000)
    
    let filtered = transactions.content
    if (startDate && endDate) {
      filtered = transactions.content.filter(t => {
        const transactionDate = new Date(t.createdAt)
        return transactionDate >= new Date(startDate) && transactionDate <= new Date(endDate)
      })
    }

    const deposits = filtered.filter(t => t.transactionType === 'DEPOSIT')
    const withdrawals = filtered.filter(t => t.transactionType === 'WITHDRAWAL')
    const transfers = filtered.filter(t => t.transactionType === 'TRANSFER')
    const completed = filtered.filter(t => t.status === 'COMPLETED')

    return {
      totalTransactions: filtered.length,
      totalDeposits: deposits.reduce((sum, t) => sum + t.amount, 0),
      totalWithdrawals: withdrawals.reduce((sum, t) => sum + t.amount, 0),
      totalTransfers: transfers.reduce((sum, t) => sum + t.amount, 0),
      totalFees: filtered.reduce((sum, t) => sum + (t.feeAmount || 0), 0),
      successRate: filtered.length ? (completed.length / filtered.length) * 100 : 0,
      avgTransactionAmount: filtered.length 
        ? filtered.reduce((sum, t) => sum + t.amount, 0) / filtered.length 
        : 0
    }
  },

  // Get monthly transaction statistics
  getMonthlyStats: async (
    userId: number,
    year: number
  ): Promise<MonthlyTransactionStats[]> => {
    // This would typically be a backend endpoint, but we'll compute it here
    const startDate = `${year}-01-01T00:00:00Z`
    const endDate = `${year}-12-31T23:59:59Z`
    
    const transactions = await transactionsApi.getTransactionsByDateRange(
      userId, startDate, endDate, 0, 1000
    )

    // Group by month
    const monthlyStats: { [key: string]: MonthlyTransactionStats } = {}
    
    for (let i = 1; i <= 12; i++) {
      const month = i.toString().padStart(2, '0')
      monthlyStats[month] = {
        month: `${year}-${month}`,
        deposits: 0,
        withdrawals: 0,
        transfers: 0,
        totalAmount: 0,
        transactionCount: 0
      }
    }

    transactions.content.forEach(transaction => {
      const month = transaction.createdAt.substring(5, 7) // Extract MM from YYYY-MM-DD
      const stats = monthlyStats[month]
      
      if (stats) {
        stats.transactionCount++
        stats.totalAmount += transaction.amount

        switch (transaction.transactionType) {
          case 'DEPOSIT':
            stats.deposits += transaction.amount
            break
          case 'WITHDRAWAL':
            stats.withdrawals += transaction.amount
            break
          case 'TRANSFER':
            stats.transfers += transaction.amount
            break
        }
      }
    })

    return Object.values(monthlyStats)
  },

  // Recent transactions (last 10)
  getRecentTransactions: async (userId: number): Promise<Transaction[]> => {
    const response = await transactionsApi.getUserTransactions(userId, 0, 10)
    return response.content
  },

  // Pending transactions count
  getPendingTransactionsCount: async (userId: number): Promise<number> => {
    return await transactionsApi.getTransactionCount(userId, 'PENDING')
  }
}
