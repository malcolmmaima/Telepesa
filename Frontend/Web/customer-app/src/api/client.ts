import axios, { AxiosError, AxiosRequestConfig, AxiosResponse } from 'axios'
import type { ApiResponse, ApiError } from '../types'
import { useAuth } from '../store/auth'

// Create axios instance with default configuration
export const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
  },
})

// Request interceptor to add auth token
api.interceptors.request.use(
  config => {
    const token = useAuth.getState().accessToken
    if (token) {
      config.headers = config.headers || {}
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// Response interceptor for error handling and token refresh
let refreshing = false
let queue: Array<() => void> = []

api.interceptors.response.use(
  (response: AxiosResponse<ApiResponse>) => {
    return response
  },
  async (error: AxiosError<ApiResponse>) => {
    const originalRequest = error.config as AxiosRequestConfig & { _retry?: boolean }

    // Handle 401 errors (unauthorized)
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true

      if (refreshing) {
        await new Promise<void>(resolve => queue.push(resolve))
        return api(originalRequest)
      }

      try {
        refreshing = true
        const { refreshToken, setSession, logout } = useAuth.getState()

        if (!refreshToken) {
          logout()
          window.location.href = '/login'
          return Promise.reject(error)
        }

        // Try to refresh token
        const response = await axios.post(
          `${api.defaults.baseURL?.replace(/\/api\/v1$/, '')}/api/v1/users/refresh`,
          {
            refreshToken,
          }
        )

        const { accessToken, refreshToken: newRefreshToken, user } = response.data
        setSession({ accessToken, refreshToken: newRefreshToken, user })

        // Process queued requests
        queue.forEach(resolve => resolve())
        queue = []

        // Retry original request with new token
        if (originalRequest.headers) {
          originalRequest.headers.Authorization = `Bearer ${accessToken}`
        }
        return api(originalRequest)
      } catch (refreshError) {
        // Refresh failed, logout user
        useAuth.getState().logout()
        queue.forEach(resolve => resolve())
        queue = []
        window.location.href = '/login'
        return Promise.reject(refreshError)
      } finally {
        refreshing = false
      }
    }

    // Transform error response
    const apiError: ApiError = {
      message: error.response?.data?.message || error.message || 'An error occurred',
      code: error.response?.data?.errors?.[0] || error.code,
      statusCode: error.response?.status || 500,
    }

    return Promise.reject(apiError)
  }
)

// Utility functions for common API operations
export const apiService = {
  // Authentication
  auth: {
    login: (data: { usernameOrEmail: string; password: string }) =>
      api.post<ApiResponse>('/users/login', data),

    register: (data: any) => api.post<ApiResponse>('/users/register', data),

    logout: () => api.post<ApiResponse>('/auth/logout'),

    refreshToken: (refreshToken: string) =>
      api.post<ApiResponse>('/users/refresh', { refreshToken }),

    forgotPassword: (email: string) => api.post<ApiResponse>('/auth/forgot-password', { email }),

    resetPassword: (data: { token: string; password: string }) =>
      api.post<ApiResponse>('/auth/reset-password', data),
  },

  // User management
  user: {
    getProfile: () => api.get<ApiResponse>('/users/profile'),

    updateProfile: (data: any) => api.put<ApiResponse>('/users/profile', data),

    changePassword: (data: { currentPassword: string; newPassword: string }) =>
      api.put<ApiResponse>('/users/change-password', data),

    uploadAvatar: (file: File) => {
      const formData = new FormData()
      formData.append('avatar', file)
      return api.post<ApiResponse>('/users/avatar', formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
      })
    },
  },

  // Accounts
  accounts: {
    getAll: () => api.get<ApiResponse>('/accounts'),

    getById: (id: number) => api.get<ApiResponse>(`/accounts/${id}`),

    create: (data: any) => api.post<ApiResponse>('/accounts', data),

    getTransactions: (accountId: number, params?: any) =>
      api.get<ApiResponse>(`/accounts/${accountId}/transactions`, { params }),
  },

  // Transactions
  transactions: {
    transfer: (data: any) => api.post<ApiResponse>('/transactions/transfer', data),

    payment: (data: any) => api.post<ApiResponse>('/transactions/payment', data),

    getReceipt: (transactionId: number) =>
      api.get<ApiResponse>(`/transactions/${transactionId}/receipt`),
  },

  // Loans
  loans: {
    getAll: () => api.get<ApiResponse>('/loans'),

    apply: (data: any) => api.post<ApiResponse>('/loans/apply', data),

    getById: (id: number) => api.get<ApiResponse>(`/loans/${id}`),

    makePayment: (loanId: number, amount: number) =>
      api.post<ApiResponse>(`/loans/${loanId}/payment`, { amount }),
  },

  // Payments & Services
  payments: {
    getServices: () => api.get<ApiResponse>('/payments/services'),

    validatePayment: (data: any) => api.post<ApiResponse>('/payments/validate', data),

    processPayment: (data: any) => api.post<ApiResponse>('/payments/process', data),
  },

  // Notifications
  notifications: {
    getAll: (params?: any) => api.get<ApiResponse>('/notifications', { params }),

    markAsRead: (id: number) => api.put<ApiResponse>(`/notifications/${id}/read`),

    markAllAsRead: () => api.put<ApiResponse>('/notifications/read-all'),
  },

  // Dashboard
  dashboard: {
    getStats: () => api.get<ApiResponse>('/dashboard/stats'),

    getRecentActivity: () => api.get<ApiResponse>('/dashboard/recent-activity'),
  },
}

export default api
