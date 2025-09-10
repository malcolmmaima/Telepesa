import { api } from './client'

// Account Types and Interfaces
export interface Account {
  id: number
  accountNumber: string
  userId: number
  accountType: 'SAVINGS' | 'CHECKING' | 'FIXED_DEPOSIT' | 'BUSINESS'
  accountName: string
  balance: number
  availableBalance: number
  minimumBalance: number
  status: 'PENDING' | 'ACTIVE' | 'FROZEN' | 'CLOSED'
  currencyCode: string
  interestRate?: number
  description?: string
  createdAt: string
  updatedAt: string
  lastTransactionAt?: string
}

export interface AccountBalance {
  accountId: number
  accountNumber: string
  balance: number
  availableBalance: number
  currencyCode: string
  lastUpdated: string
}

export interface CreateAccountRequest {
  accountType: Account['accountType']
  accountName: string
  minimumBalance?: number
  initialDeposit?: number
  currencyCode: string
  description?: string
}

export interface UpdateAccountRequest {
  accountName?: string
  description?: string
  minimumBalance?: number
}

export interface CreditDebitRequest {
  amount: number
  description: string
}

export interface TransferRequest {
  toAccountId: number
  amount: number
  description: string
}

export interface AccountStatistics {
  totalAccounts: number
  totalBalance: number
  activeAccounts: number
  frozenAccounts: number
  accountsByType: Record<string, number>
  averageBalance: number
}

export interface PageResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
  first: boolean
  last: boolean
}

// Account API Service
export const accountsApi = {
  // Get user accounts
  getUserAccounts: async (
    userId: number,
    page = 0,
    size = 20,
    sortBy = 'createdAt',
    sortDir = 'desc'
  ): Promise<PageResponse<Account>> => {
    const response = await api.get(`/accounts/user/${userId}`, {
      params: { page, size, sortBy, sortDir }
    })
    return response.data
  },

  // Get user active accounts only
  getUserActiveAccounts: async (userId: number): Promise<Account[]> => {
    const response = await api.get(`/accounts/user/${userId}/active`)
    return response.data
  },

  // Get account by ID
  getAccount: async (accountId: number): Promise<Account> => {
    const response = await api.get(`/accounts/${accountId}`)
    return response.data
  },

  // Get account by account number
  getAccountByNumber: async (accountNumber: string): Promise<Account> => {
    const response = await api.get(`/accounts/number/${accountNumber}`)
    return response.data
  },

  // Create new account
  createAccount: async (userId: number, request: CreateAccountRequest): Promise<Account> => {
    const response = await api.post('/accounts', {
      ...request,
      userId
    })
    return response.data
  },

  // Update account
  updateAccount: async (accountId: number, request: UpdateAccountRequest): Promise<Account> => {
    const response = await api.put(`/accounts/${accountId}`, request)
    return response.data
  },

  // Account operations
  activateAccount: async (accountId: number): Promise<Account> => {
    const response = await api.post(`/accounts/${accountId}/activate`)
    return response.data
  },

  freezeAccount: async (accountId: number): Promise<Account> => {
    const response = await api.post(`/accounts/${accountId}/freeze`)
    return response.data
  },

  unfreezeAccount: async (accountId: number): Promise<Account> => {
    const response = await api.post(`/accounts/${accountId}/unfreeze`)
    return response.data
  },

  closeAccount: async (accountId: number): Promise<Account> => {
    const response = await api.post(`/accounts/${accountId}/close`)
    return response.data
  },

  // Balance operations
  getAccountBalance: async (accountId: number): Promise<AccountBalance> => {
    const response = await api.get(`/accounts/${accountId}/balance`)
    return response.data
  },

  creditAccount: async (accountId: number, request: CreditDebitRequest): Promise<Account> => {
    const response = await api.post(`/accounts/${accountId}/credit`, request)
    return response.data
  },

  debitAccount: async (accountId: number, request: CreditDebitRequest): Promise<Account> => {
    const response = await api.post(`/accounts/${accountId}/debit`, request)
    return response.data
  },

  // Transfer between accounts
  transferBetweenAccounts: async (fromAccountId: number, request: TransferRequest): Promise<Account> => {
    const response = await api.post(`/accounts/${fromAccountId}/transfer`, request)
    return response.data
  },

  // Get user total balance
  getUserTotalBalance: async (userId: number): Promise<number> => {
    const response = await api.get(`/accounts/user/${userId}/total-balance`)
    return response.data
  },

  // Search and filter accounts
  getAccountsByStatus: async (
    status: Account['status'],
    page = 0,
    size = 20
  ): Promise<PageResponse<Account>> => {
    const response = await api.get(`/accounts/status/${status}`, {
      params: { page, size }
    })
    return response.data
  },

  getAccountsByType: async (
    accountType: Account['accountType'],
    page = 0,
    size = 20
  ): Promise<PageResponse<Account>> => {
    const response = await api.get(`/accounts/type/${accountType}`, {
      params: { page, size }
    })
    return response.data
  },

  searchAccounts: async (
    query: string,
    page = 0,
    size = 20
  ): Promise<PageResponse<Account>> => {
    const response = await api.get('/accounts/search', {
      params: { q: query, page, size }
    })
    return response.data
  },

  // Analytics and reports
  getAccountStatistics: async (): Promise<AccountStatistics> => {
    const response = await api.get('/accounts/statistics')
    return response.data
  },

  getAccountsBelowMinimumBalance: async (): Promise<Account[]> => {
    const response = await api.get('/accounts/below-minimum-balance')
    return response.data
  },

  getDormantAccounts: async (days = 90): Promise<Account[]> => {
    const response = await api.get('/accounts/dormant', {
      params: { days }
    })
    return response.data
  }
}
