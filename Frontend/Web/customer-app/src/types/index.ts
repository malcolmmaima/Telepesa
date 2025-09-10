// User and Authentication Types
export interface User {
  id: number
  username: string
  email: string
  firstName?: string
  lastName?: string
  phoneNumber?: string
  dateOfBirth?: string
  profilePicture?: string
  kycStatus: 'pending' | 'verified' | 'rejected'
  accountStatus: 'active' | 'suspended' | 'closed'
  createdAt: string
  updatedAt: string
}

export interface LoginRequest {
  usernameOrEmail: string
  password: string
}

export interface LoginResponse {
  accessToken: string
  refreshToken: string
  user: User
}

export interface RegisterRequest {
  username: string
  email: string
  password: string
  firstName: string
  lastName: string
  phoneNumber: string
  dateOfBirth?: string
}

// Account Types
export interface Account {
  id: number
  accountNumber: string
  accountType: 'savings' | 'current' | 'loan' | 'fixed_deposit'
  balance: number
  availableBalance: number
  currency: string
  status: 'active' | 'blocked' | 'closed'
  createdAt: string
  updatedAt: string
}

// Transaction Types
export interface Transaction {
  id: number
  accountId: number
  type: 'credit' | 'debit'
  category: 'transfer' | 'deposit' | 'withdrawal' | 'payment' | 'loan' | 'fee'
  amount: number
  balance: number
  description: string
  reference: string
  status: 'completed' | 'pending' | 'failed' | 'cancelled'
  recipientName?: string
  recipientAccount?: string
  charges?: number
  createdAt: string
}

export interface TransferRequest {
  fromAccountId: number
  toAccount: string
  amount: number
  description?: string
  recipientName?: string
}

export interface PaymentRequest {
  accountId: number
  payeeCode: string
  amount: number
  reference?: string
  description?: string
}

// Loan Types
export interface Loan {
  id: number
  accountId: number
  loanType: 'personal' | 'business' | 'asset' | 'emergency'
  principal: number
  interestRate: number
  termMonths: number
  monthlyPayment: number
  balance: number
  status: 'active' | 'paid' | 'defaulted' | 'pending'
  nextPaymentDate: string
  createdAt: string
}

export interface LoanApplication {
  accountId: number
  loanType: 'personal' | 'business' | 'asset' | 'emergency'
  amount: number
  termMonths: number
  purpose: string
  monthlyIncome?: number
  employmentStatus?: string
  collateral?: string
}

// Payment and Service Types
export interface PaymentService {
  code: string
  name: string
  category: 'utility' | 'mobile' | 'tv' | 'internet' | 'government'
  icon: string
  minAmount: number
  maxAmount: number
  fields: PaymentField[]
}

export interface PaymentField {
  name: string
  label: string
  type: 'text' | 'number' | 'select'
  required: boolean
  options?: string[]
  validation?: {
    pattern?: string
    minLength?: number
    maxLength?: number
  }
}

// Notification Types
export interface Notification {
  id: number
  userId: number
  title: string
  message: string
  type: 'info' | 'success' | 'warning' | 'error'
  read: boolean
  createdAt: string
}

// API Response Types
export interface ApiResponse<T = any> {
  success: boolean
  data?: T
  message?: string
  errors?: string[]
}

export interface PaginatedResponse<T = any> {
  success: boolean
  data: T[]
  pagination: {
    page: number
    limit: number
    total: number
    totalPages: number
  }
  message?: string
}

// Form Types
export interface FormField {
  name: string
  value: any
  error?: string
  touched: boolean
}

// Dashboard Types
export interface DashboardStats {
  totalBalance: number
  monthlyIncome: number
  monthlyExpenses: number
  savingsGoalProgress: number
  activeLoans: number
  recentTransactionsCount: number
}

export interface QuickAction {
  id: string
  label: string
  icon: string
  description: string
  path: string
  enabled: boolean
}

// Settings Types
export interface UserSettings {
  notifications: {
    email: boolean
    sms: boolean
    push: boolean
    transactionAlerts: boolean
    loginAlerts: boolean
    marketingEmails: boolean
  }
  security: {
    twoFactorAuth: boolean
    sessionTimeout: number
    loginNotifications: boolean
  }
  preferences: {
    language: string
    currency: string
    theme: 'light' | 'dark' | 'auto'
    dateFormat: string
  }
}

// Error Types
export interface ApiError {
  message: string
  code?: string
  field?: string
  statusCode: number
}

// Component Props Types
export interface BaseComponentProps {
  className?: string
  children?: React.ReactNode
}

export interface ButtonProps extends BaseComponentProps {
  variant?: 'primary' | 'secondary' | 'outline' | 'ghost' | 'danger'
  size?: 'sm' | 'md' | 'lg'
  disabled?: boolean
  loading?: boolean
  onClick?: (event?: React.MouseEvent<HTMLButtonElement>) => void
  type?: 'button' | 'submit' | 'reset'
}

export interface InputProps extends BaseComponentProps {
  label?: string
  placeholder?: string
  type?: string
  error?: string
  required?: boolean
  disabled?: boolean
  value?: any
  onChange?: (event: React.ChangeEvent<HTMLInputElement>) => void
  onBlur?: (event: React.FocusEvent<HTMLInputElement>) => void
  name?: string
  id?: string
  min?: string | number
  max?: string | number
  step?: string | number
  minLength?: number
  maxLength?: number
}

export interface CardProps extends BaseComponentProps {
  title?: string
  description?: string
  actions?: React.ReactNode
  padding?: boolean
  onClick?: (event?: React.MouseEvent<HTMLDivElement>) => void
}

// State Types
export interface AuthState {
  accessToken: string | null
  refreshToken: string | null
  user: User | null
  isAuthenticated: boolean
  isLoading: boolean
  error: string | null
}

export interface AppState {
  accounts: Account[]
  selectedAccount: Account | null
  transactions: Transaction[]
  loans: Loan[]
  notifications: Notification[]
  isLoading: boolean
  error: string | null
}
