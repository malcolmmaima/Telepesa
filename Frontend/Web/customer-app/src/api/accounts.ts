import { api } from './client'

// Account Types and Interfaces
export interface Account {
  id: number
  accountNumber: string
  userId: number
  accountType: 'SAVINGS' | 'CHECKING' | 'FIXED_DEPOSIT' | 'BUSINESS'
  accountName?: string
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
  size?: number
  number?: number
  first?: boolean
  last?: boolean
  pageSize?: number
  currentPage?: number
  hasNext?: boolean
  hasPrevious?: boolean
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
  ): Promise<Account[]> => {
    const response = await api.get(`/accounts/user/${userId}`, {
      params: { page, size, sortBy, sortDir },
    })
    const data = response.data
    // Support multiple pagination shapes and direct arrays
    if (Array.isArray(data)) return data
    if (Array.isArray(data?.data)) return data.data
    if (Array.isArray(data?.content)) return data.content
    if (Array.isArray(data?.result)) return data.result
    return []
  },

  // Get account by ID
  getAccount: async (accountId: number): Promise<Account> => {
    const response = await api.get(`/accounts/${accountId}`)
    // Backend may return data in {data: Account} format, extract the account object
    return response.data?.data || response.data
  },

  // Get account by account number
  getAccountByNumber: async (accountNumber: string): Promise<Account> => {
    const response = await api.get(`/accounts/number/${accountNumber}`)
    // Backend may return data in {data: Account} format, extract the account object
    return response.data?.data || response.data
  },

  // Update account
  updateAccount: async (accountId: number, request: UpdateAccountRequest): Promise<Account> => {
    const response = await api.put(`/accounts/${accountId}`, request)
    // Backend may return data in {data: Account} format, extract the account object
    return response.data?.data || response.data
  },

  // Account operations
  activateAccount: async (accountId: number): Promise<Account> => {
    const response = await api.post(`/accounts/${accountId}/activate`)
    // Backend may return data in {data: Account} format, extract the account object
    return response.data?.data || response.data
  },

  freezeAccount: async (accountId: number): Promise<Account> => {
    const response = await api.post(`/accounts/${accountId}/freeze`)
    // Backend may return data in {data: Account} format, extract the account object
    return response.data?.data || response.data
  },

  unfreezeAccount: async (accountId: number): Promise<Account> => {
    const response = await api.post(`/accounts/${accountId}/unfreeze`)
    // Backend may return data in {data: Account} format, extract the account object
    return response.data?.data || response.data
  },

  closeAccount: async (accountId: number): Promise<Account> => {
    const response = await api.post(`/accounts/${accountId}/close`)
    // Backend may return data in {data: Account} format, extract the account object
    return response.data?.data || response.data
  },

  // Balance operations
  getAccountBalance: async (accountId: number): Promise<AccountBalance> => {
    const response = await api.get(`/accounts/${accountId}/balance`)
    // Backend may return data in {data: AccountBalance} format, extract the balance object
    return response.data?.data || response.data
  },

  creditAccount: async (accountId: number, request: CreditDebitRequest): Promise<Account> => {
    const response = await api.post(`/accounts/${accountId}/credit`, request)
    // Backend may return data in {data: Account} format, extract the account object
    return response.data?.data || response.data
  },

  debitAccount: async (accountId: number, request: CreditDebitRequest): Promise<Account> => {
    const response = await api.post(`/accounts/${accountId}/debit`, request)
    // Backend may return data in {data: Account} format, extract the account object
    return response.data?.data || response.data
  },

  // Transfer between accounts
  transferBetweenAccounts: async (
    fromAccountId: number,
    request: TransferRequest
  ): Promise<Account> => {
    const response = await api.post(`/accounts/${fromAccountId}/transfer`, request)
    // Backend may return data in {data: Account} format, extract the account object
    return response.data?.data || response.data
  },

  // Get user total balance
  getUserTotalBalance: async (
    userId: number
  ): Promise<{
    totalBalance: number
    totalAvailableBalance: number
    accountCount: number
    currencyCode: string
  }> => {
    const response = await api.get(`/accounts/user/${userId}/total-balance`)
    // Backend returns balance summary object
    return (
      response.data || {
        totalBalance: 0,
        totalAvailableBalance: 0,
        accountCount: 0,
        currencyCode: 'KES',
      }
    )
  },

  // Search and filter accounts
  getAccountsByStatus: async (
    status: Account['status'],
    page = 0,
    size = 20
  ): Promise<PageResponse<Account>> => {
    const response = await api.get(`/accounts/status/${status}`, {
      params: { page, size },
    })
    // Backend may return data in {data: PageResponse} format, extract the page response
    return response.data?.data || response.data
  },

  getAccountsByType: async (
    accountType: Account['accountType'],
    page = 0,
    size = 20
  ): Promise<PageResponse<Account>> => {
    const response = await api.get(`/accounts/type/${accountType}`, {
      params: { page, size },
    })
    // Backend may return data in {data: PageResponse} format, extract the page response
    return response.data?.data || response.data
  },

  searchAccounts: async (query: string, page = 0, size = 20): Promise<PageResponse<Account>> => {
    const response = await api.get('/accounts/search', {
      params: { q: query, page, size },
    })
    // Backend may return data in {data: PageResponse} format, extract the page response
    return response.data?.data || response.data
  },

  // Analytics and reports
  getAccountStatistics: async (): Promise<AccountStatistics> => {
    const response = await api.get('/accounts/statistics')
    // Backend may return data in {data: AccountStatistics} format, extract the statistics object
    return response.data?.data || response.data
  },

  getAccountsBelowMinimumBalance: async (): Promise<Account[]> => {
    const response = await api.get('/accounts/below-minimum-balance')
    // Backend returns data in {data: Account[]} format, extract the accounts array
    return Array.isArray(response.data?.data) ? response.data.data : []
  },

  getDormantAccounts: async (days = 90): Promise<Account[]> => {
    const response = await api.get('/accounts/dormant', {
      params: { days },
    })
    // Backend returns data in {data: Account[]} format, extract the accounts array
    return Array.isArray(response.data?.data) ? response.data.data : []
  },
}
