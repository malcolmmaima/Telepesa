import { api } from './client'

// Loan Types and Interfaces
export interface Loan {
  id: number
  userId: number
  accountId: number
  loanType: 'PERSONAL' | 'BUSINESS' | 'EMERGENCY' | 'ASSET'
  principal: number
  interestRate: number
  termMonths: number
  monthlyPayment: number
  remainingBalance: number
  totalAmount: number
  status: 'PENDING' | 'APPROVED' | 'REJECTED' | 'ACTIVE' | 'COMPLETED' | 'DEFAULTED'
  applicationDate: string
  approvalDate?: string
  disbursementDate?: string
  nextPaymentDate?: string
  purpose: string
  collateral?: string
  monthlyIncome?: number
  employmentStatus?: string
  creditScore?: number
  createdAt: string
  updatedAt: string
}

export interface LoanApplication {
  loanType: Loan['loanType']
  requestedAmount: number
  termMonths: number
  purpose: string
  monthlyIncome: number
  employmentStatus: string
  employerName?: string
  collateral?: string
  collateralValue?: number
  accountId: number
}

export interface LoanProduct {
  id: number
  name: string
  loanType: Loan['loanType']
  minAmount: number
  maxAmount: number
  interestRate?: number | null
  minInterestRate: number
  maxInterestRate: number
  minTermMonths: number
  maxTermMonths: number
  processingFee?: number | null
  processingFeePercentage?: number | null
  requiresCollateral?: boolean | null
  maxLtvRatio?: number | null
  eligibilityCriteria?: string | null
  requiredDocuments?: string | null
  description: string
  requirements: string[]
  features: string[]
  currency: string
  isActive: boolean
  createdAt?: string | null
  updatedAt?: string | null
  effectiveRate?: number | null
  termRange?: string | null
  amountRange?: string | null
}

export interface LoanPayment {
  id: number
  loanId: number
  amount: number
  paymentDate: string
  principalAmount: number
  interestAmount: number
  remainingBalance: number
  status: 'PENDING' | 'COMPLETED' | 'FAILED'
  paymentMethod: string
  reference: string
}

export interface LoanCalculation {
  requestedAmount: number
  interestRate: number
  termMonths: number
  monthlyPayment: number
  totalInterest: number
  totalAmount: number
  schedule: {
    month: number
    principalAmount: number
    interestAmount: number
    totalPayment: number
    remainingBalance: number
  }[]
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

// Loans API Service
export const loansApi = {
  // Get loan products
  getLoanProducts: async (): Promise<LoanProduct[]> => {
    const response = await api.get('/loans/products')
    return Array.isArray(response.data?.data)
      ? response.data.data
      : Array.isArray(response.data)
        ? response.data
        : []
  },

  // Get loan product by type
  getLoanProductByType: async (loanType: Loan['loanType']): Promise<LoanProduct> => {
    const response = await api.get(`/loans/products/${loanType}`)
    return response.data
  },

  // Calculate loan terms
  calculateLoan: async (
    amount: number,
    termMonths: number,
    loanType: Loan['loanType']
  ): Promise<LoanCalculation> => {
    const response = await api.post('/loans/calculate', {
      amount,
      termMonths,
      loanType,
    })
    return response.data
  },

  // Apply for loan
  applyForLoan: async (application: LoanApplication): Promise<Loan> => {
    // Map loan type to product ID based on the loan products we know exist
    const getLoanProductId = (loanType: string): number => {
      switch (loanType) {
        case 'PERSONAL':
          return 1
        case 'BUSINESS':
          return 2
        case 'EMERGENCY':
          return 3
        default:
          return 1
      }
    }

    // Convert frontend LoanApplication to backend CreateLoanRequest format
    const createLoanRequest = {
      userId: application.accountId, // Will be set by backend from JWT token
      accountId: application.accountId,
      loanProductId: getLoanProductId(application.loanType),
      loanType: application.loanType,
      principalAmount: application.requestedAmount,
      termInMonths: application.termMonths,
      interestRate: 12.5, // Default rate, will be calculated by backend
      purpose: application.purpose,
      monthlyIncome: application.monthlyIncome,
      collateralDescription: application.collateral || '',
      collateralValue: application.collateralValue || 0,
      additionalNotes: `Employment: ${application.employmentStatus}${application.employerName ? `, Employer: ${application.employerName}` : ''}`,
      agreeToTerms: true,
    }

    const response = await api.post('/loans', createLoanRequest)
    return response.data
  },

  // Get user loans
  getUserLoans: async (
    userId: number,
    page = 0,
    size = 20,
    status?: Loan['status']
  ): Promise<PageResponse<Loan>> => {
    const params: any = { page, size }
    if (status) params.status = status

    const response = await api.get(`/loans/user/${userId}`, { params })
    return response.data
  },

  // Get loan by ID
  getLoan: async (loanId: number): Promise<Loan> => {
    const response = await api.get(`/loans/${loanId}`)
    return response.data
  },

  // Get loan payments history
  getLoanPayments: async (
    loanId: number,
    page = 0,
    size = 50
  ): Promise<PageResponse<LoanPayment>> => {
    const response = await api.get(`/loans/${loanId}/payments`, {
      params: { page, size },
    })
    return response.data
  },

  // Make loan payment
  makeLoanPayment: async (
    loanId: number,
    amount: number,
    accountId: number,
    description?: string
  ): Promise<LoanPayment> => {
    const response = await api.post(`/loans/${loanId}/payment`, {
      amount,
      accountId,
      description,
    })
    return response.data
  },

  // Get loan summary for user
  getUserLoanSummary: async (
    userId: number
  ): Promise<{
    totalLoans: number
    activeLoans: number
    totalBorrowed: number
    totalOutstanding: number
    monthlyPayments: number
    nextPaymentDate?: string
    creditScore?: number
  }> => {
    const response = await api.get(`/loans/user/${userId}/summary`)
    return response.data
  },

  // Check loan eligibility
  checkEligibility: async (
    amount: number,
    loanType: Loan['loanType'],
    monthlyIncome: number
  ): Promise<{
    eligible: boolean
    maxAmount: number
    recommendedAmount: number
    interestRate: number
    reasons: string[]
  }> => {
    const response = await api.post('/loans/eligibility', {
      amount,
      loanType,
      monthlyIncome,
    })
    return response.data
  },

  // Upload loan documents
  uploadLoanDocument: async (
    loanId: number,
    documentType: string,
    file: File
  ): Promise<{ documentId: number; url: string }> => {
    const formData = new FormData()
    formData.append('file', file)
    formData.append('documentType', documentType)

    const response = await api.post(`/loans/${loanId}/documents`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
    return response.data
  },

  // Get loan documents
  getLoanDocuments: async (
    loanId: number
  ): Promise<
    {
      id: number
      documentType: string
      fileName: string
      url: string
      uploadedAt: string
    }[]
  > => {
    const response = await api.get(`/loans/${loanId}/documents`)
    return response.data
  },
}
