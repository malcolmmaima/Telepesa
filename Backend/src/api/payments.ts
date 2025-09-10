import { api } from './client'

// Payment Types and Interfaces
export interface PaymentProvider {
  id: number
  providerCode: string
  providerName: string
  category: 'UTILITY' | 'MOBILE' | 'TV' | 'INTERNET' | 'GOVERNMENT' | 'INSURANCE'
  description: string
  logoUrl?: string
  fields: PaymentField[]
  minAmount: number
  maxAmount: number
  feeStructure: FeeStructure
  active: boolean
  country: string
}

export interface PaymentField {
  fieldName: string
  fieldLabel: string
  fieldType: 'TEXT' | 'NUMBER' | 'SELECT' | 'EMAIL' | 'PHONE'
  required: boolean
  options?: string[]
  validation?: FieldValidation
  placeholder?: string
  description?: string
}

export interface FieldValidation {
  pattern?: string
  minLength?: number
  maxLength?: number
  min?: number
  max?: number
}

export interface FeeStructure {
  type: 'FIXED' | 'PERCENTAGE' | 'TIERED'
  value: number
  minFee?: number
  maxFee?: number
  tiers?: FeeTier[]
}

export interface FeeTier {
  minAmount: number
  maxAmount: number
  fee: number
}

export interface BillPayment {
  id: number
  paymentId: string
  userId: number
  fromAccountId: number
  providerCode: string
  providerName: string
  amount: number
  fee: number
  totalAmount: number
  currency: string
  customerReference: string
  billReference?: string
  status: 'PENDING' | 'PROCESSING' | 'COMPLETED' | 'FAILED' | 'CANCELLED'
  paymentData: Record<string, any>
  description: string
  receiptNumber?: string
  failureReason?: string
  createdAt: string
  updatedAt: string
  completedAt?: string
}

export interface CreateBillPaymentRequest {
  fromAccountId: number
  providerCode: string
  amount: number
  currency?: string
  customerReference: string
  paymentData: Record<string, any>
  description: string
  savePayee?: boolean
}

export interface SavedPayee {
  id: number
  userId: number
  providerCode: string
  providerName: string
  nickname: string
  customerReference: string
  paymentData: Record<string, any>
  lastUsed?: string
  createdAt: string
}

export interface CreateSavedPayeeRequest {
  providerCode: string
  nickname: string
  customerReference: string
  paymentData: Record<string, any>
}

export interface BillPaymentFeeRequest {
  fromAccountId: number
  providerCode: string
  amount: number
  currency?: string
}

export interface BillPaymentFeeResponse {
  fee: number
  totalAmount: number
  estimatedProcessingTime: string
}

export interface PaymentValidationRequest {
  providerCode: string
  customerReference: string
  paymentData: Record<string, any>
}

export interface PaymentValidationResponse {
  valid: boolean
  customerName?: string
  billAmount?: number
  dueDate?: string
  message?: string
}

export interface PaymentStats {
  totalPayments: number
  totalAmount: number
  totalFees: number
  successfulPayments: number
  failedPayments: number
  averageAmount: number
  mostUsedProvider?: string
  recentPayments: number
}

// Bill Payment API Service
export const paymentsApi = {
  // Get available payment providers
  getPaymentProviders: async (category?: string, country = 'KE'): Promise<PaymentProvider[]> => {
    const params: any = { country }
    if (category) params.category = category
    
    const response = await api.get('/bill-payments/providers', { params })
    return response.data
  },

  // Get specific payment provider
  getPaymentProvider: async (providerCode: string): Promise<PaymentProvider> => {
    const response = await api.get(`/bill-payments/providers/${providerCode}`)
    return response.data
  },

  // Create bill payment
  createBillPayment: async (request: CreateBillPaymentRequest): Promise<BillPayment> => {
    const response = await api.post('/bill-payments', request)
    return response.data
  },

  // Get bill payment by ID
  getBillPayment: async (id: number): Promise<BillPayment> => {
    const response = await api.get(`/bill-payments/${id}`)
    return response.data
  },

  // Get bill payment by payment ID
  getBillPaymentByPaymentId: async (paymentId: string): Promise<BillPayment> => {
    const response = await api.get(`/bill-payments/payment-id/${paymentId}`)
    return response.data
  },

  // Get user bill payments
  getUserBillPayments: async (
    userId: number,
    page = 0,
    size = 20,
    status?: string,
    providerCode?: string,
    startDate?: string,
    endDate?: string
  ): Promise<{
    content: BillPayment[]
    totalElements: number
    totalPages: number
    size: number
    number: number
  }> => {
    const params: any = { page, size }
    if (status) params.status = status
    if (providerCode) params.providerCode = providerCode
    if (startDate) params.startDate = startDate
    if (endDate) params.endDate = endDate
    
    const response = await api.get(`/bill-payments/user/${userId}`, { params })
    return response.data
  },

  // Calculate bill payment fee
  calculateBillPaymentFee: async (request: BillPaymentFeeRequest): Promise<BillPaymentFeeResponse> => {
    const response = await api.post('/bill-payments/calculate-fee', request)
    return response.data
  },

  // Validate payment details
  validatePaymentDetails: async (request: PaymentValidationRequest): Promise<PaymentValidationResponse> => {
    const response = await api.post('/bill-payments/validate', request)
    return response.data
  },

  // Cancel bill payment (if pending)
  cancelBillPayment: async (id: number): Promise<BillPayment> => {
    const response = await api.post(`/bill-payments/${id}/cancel`)
    return response.data
  },

  // Retry failed bill payment
  retryBillPayment: async (id: number): Promise<BillPayment> => {
    const response = await api.post(`/bill-payments/${id}/retry`)
    return response.data
  },

  // Saved Payees Management
  getSavedPayees: async (userId: number, providerCode?: string): Promise<SavedPayee[]> => {
    const params: any = {}
    if (providerCode) params.providerCode = providerCode
    
    const response = await api.get(`/bill-payments/user/${userId}/payees`, { params })
    return response.data
  },

  createSavedPayee: async (
    userId: number,
    request: CreateSavedPayeeRequest
  ): Promise<SavedPayee> => {
    const response = await api.post(`/bill-payments/user/${userId}/payees`, request)
    return response.data
  },

  updateSavedPayee: async (
    payeeId: number,
    request: CreateSavedPayeeRequest
  ): Promise<SavedPayee> => {
    const response = await api.put(`/bill-payments/payees/${payeeId}`, request)
    return response.data
  },

  deleteSavedPayee: async (payeeId: number): Promise<void> => {
    await api.delete(`/bill-payments/payees/${payeeId}`)
  },

  // Get payment statistics
  getPaymentStats: async (
    userId: number,
    startDate?: string,
    endDate?: string
  ): Promise<PaymentStats> => {
    const params: any = {}
    if (startDate) params.startDate = startDate
    if (endDate) params.endDate = endDate
    
    const response = await api.get(`/bill-payments/user/${userId}/stats`, { params })
    return response.data
  },

  // Search payments
  searchPayments: async (
    userId: number,
    query: string,
    page = 0,
    size = 20
  ): Promise<{
    content: BillPayment[]
    totalElements: number
    totalPages: number
  }> => {
    const response = await api.get(`/bill-payments/user/${userId}/search`, {
      params: { query, page, size }
    })
    return response.data
  },

  // Get payment receipt
  getPaymentReceipt: async (paymentId: string): Promise<{
    receiptData: any
    downloadUrl?: string
  }> => {
    const response = await api.get(`/bill-payments/receipt/${paymentId}`)
    return response.data
  }
}
