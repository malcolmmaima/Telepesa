import { api } from './client'
import type { PageResponse } from './accounts'

// Transfer Types
export interface Transfer {
  id: number
  transferId: string
  fromAccountId: number
  toAccountId?: number
  toAccountNumber?: string
  toBankCode?: string
  toBankName?: string
  userId: number
  recipientName: string
  recipientPhone?: string
  recipientEmail?: string
  amount: number
  fee: number
  totalAmount: number
  currency: string
  transferType: 'INTERNAL' | 'EXTERNAL' | 'BANK_TRANSFER' | 'MOBILE_MONEY'
  status: 'PENDING' | 'PROCESSING' | 'COMPLETED' | 'FAILED' | 'CANCELLED'
  description: string
  reference?: string
  exchangeRate?: number
  balanceAfter?: number
  createdAt: string
  updatedAt: string
  completedAt?: string
  failureReason?: string
}

export interface CreateTransferRequest {
  fromAccountId: number
  toAccountId?: number
  toAccountNumber?: string
  toBankCode?: string
  recipientName: string
  recipientPhone?: string
  recipientEmail?: string
  amount: number
  currency?: string
  transferType: Transfer['transferType']
  description: string
  saveRecipient?: boolean
}

export interface SavedRecipient {
  id: number
  userId: number
  recipientName: string
  accountNumber?: string
  bankCode?: string
  bankName?: string
  phoneNumber?: string
  email?: string
  recipientType: 'BANK_ACCOUNT' | 'MOBILE_MONEY' | 'INTERNAL_ACCOUNT'
  nickname?: string
  createdAt: string
}

export interface CreateRecipientRequest {
  recipientName: string
  accountNumber?: string
  bankCode?: string
  phoneNumber?: string
  email?: string
  recipientType: SavedRecipient['recipientType']
  nickname?: string
}

export interface TransferFeeRequest {
  fromAccountId: number
  toAccountId?: number
  toBankCode?: string
  amount: number
  transferType: Transfer['transferType']
  currency?: string
}

export interface TransferFeeResponse {
  fee: number
  totalAmount: number
  exchangeRate?: number
  estimatedArrival: string
}

export interface BankInfo {
  bankCode: string
  bankName: string
  country: string
  swiftCode?: string
  active: boolean
}

export interface TransferLimits {
  dailyLimit: number
  monthlyLimit: number
  perTransactionLimit: number
  dailyUsed: number
  monthlyUsed: number
  remainingDaily: number
  remainingMonthly: number
}

// Transfer API Service
export const transfersApi = {
  // Create transfer
  createTransfer: async (request: CreateTransferRequest): Promise<Transfer> => {
    const response = await api.post('/transfers', request)
    // Backend may return data in {data: Transfer} format, extract the transfer object
    return response.data?.data || response.data
  },

  // Get transfer by ID
  getTransfer: async (id: number): Promise<Transfer> => {
    const response = await api.get(`/transfers/${id}`)
    // Backend may return data in {data: Transfer} format, extract the transfer object
    return response.data?.data || response.data
  },

  // Get transfer by transfer ID
  getTransferByTransferId: async (transferId: string): Promise<Transfer> => {
    const response = await api.get(`/transfers/transfer-id/${transferId}`)
    // Backend may return data in {data: Transfer} format, extract the transfer object
    return response.data?.data || response.data
  },

  // Get user transfers
  getUserTransfers: async (
    userId: number,
    page = 0,
    size = 20,
    status?: string,
    transferType?: string
  ): Promise<PageResponse<Transfer>> => {
    const params: any = { page, size }
    if (status) params.status = status
    if (transferType) params.transferType = transferType

    const response = await api.get(`/transfers/user/${userId}`, { params })
    // Backend may return data in {data: PageResponse} format, extract the page response
    return response.data?.data || response.data
  },

  // Get account transfers (both sent and received)
  getAccountTransfers: async (
    accountId: number,
    page = 0,
    size = 20
  ): Promise<PageResponse<Transfer>> => {
    const response = await api.get(`/transfers/account/${accountId}`, {
      params: { page, size },
    })
    return response.data
  },

  // Calculate transfer fee
  calculateTransferFee: async (request: TransferFeeRequest): Promise<TransferFeeResponse> => {
    const response = await api.post('/transfers/calculate-fee', request)
    // Backend may return data in {data: TransferFeeResponse} format, extract the fee response
    return response.data?.data || response.data
  },

  // Cancel transfer (if pending)
  cancelTransfer: async (id: number): Promise<Transfer> => {
    const response = await api.post(`/transfers/${id}/cancel`)
    // Backend may return data in {data: Transfer} format, extract the transfer object
    return response.data?.data || response.data
  },

  // Retry failed transfer
  retryTransfer: async (id: number): Promise<Transfer> => {
    const response = await api.post(`/transfers/${id}/retry`)
    // Backend may return data in {data: Transfer} format, extract the transfer object
    return response.data?.data || response.data
  },

  // Get transfer limits for user
  getTransferLimits: async (userId: number): Promise<TransferLimits> => {
    const response = await api.get(`/transfers/user/${userId}/limits`)
    return response.data
  },

  // Saved Recipients Management
  getSavedRecipients: async (userId: number): Promise<SavedRecipient[]> => {
    const response = await api.get(`/transfers/user/${userId}/recipients`)
    // Backend may return data in {data: SavedRecipient[]} format, extract the recipients array
    return Array.isArray(response.data?.data) ? response.data.data : (Array.isArray(response.data) ? response.data : [])
  },

  createSavedRecipient: async (
    userId: number,
    request: CreateRecipientRequest
  ): Promise<SavedRecipient> => {
    const response = await api.post(`/transfers/user/${userId}/recipients`, request)
    return response.data
  },

  updateSavedRecipient: async (
    recipientId: number,
    request: CreateRecipientRequest
  ): Promise<SavedRecipient> => {
    const response = await api.put(`/transfers/recipients/${recipientId}`, request)
    return response.data
  },

  deleteSavedRecipient: async (recipientId: number): Promise<void> => {
    await api.delete(`/transfers/recipients/${recipientId}`)
  },

  // Bank Information
  getSupportedBanks: async (country = 'KE'): Promise<BankInfo[]> => {
    const response = await api.get('/transfers/banks', {
      params: { country },
    })
    // Backend may return data in {data: BankInfo[]} format, extract the banks array
    return Array.isArray(response.data?.data) ? response.data.data : (Array.isArray(response.data) ? response.data : [])
  },

  getBankInfo: async (bankCode: string): Promise<BankInfo> => {
    const response = await api.get(`/transfers/banks/${bankCode}`)
    return response.data
  },

  // Validate account number
  validateAccountNumber: async (
    bankCode: string,
    accountNumber: string
  ): Promise<{ valid: boolean; accountName?: string; message?: string }> => {
    const response = await api.post('/transfers/validate-account', {
      bankCode,
      accountNumber,
    })
    return response.data
  },

  // Transfer analytics
  getTransferStats: async (
    userId: number,
    startDate?: string,
    endDate?: string
  ): Promise<{
    totalTransfers: number
    totalAmount: number
    totalFees: number
    successfulTransfers: number
    failedTransfers: number
    averageAmount: number
    mostUsedRecipient?: string
    mostUsedBank?: string
  }> => {
    const params: any = {}
    if (startDate) params.startDate = startDate
    if (endDate) params.endDate = endDate

    const response = await api.get(`/transfers/user/${userId}/stats`, { params })
    return response.data
  },
}
