import { api } from './client'

export interface TransactionPin {
  id: number
  userId: number
  set: boolean
  createdAt: string
  updatedAt: string
}

export interface SecurityQuestion {
  id: number
  question: string
  category: string
}

export interface UserSecurityQuestion {
  id: number
  userId: number
  questionId: number
  question: string
  answer: string
  createdAt: string
}

export interface CreatePinRequest {
  pin: string
}

export interface VerifyPinRequest {
  pin: string
}

export interface ChangePinRequest {
  currentPin: string
  newPin: string
}

export interface SetSecurityQuestionsRequest {
  questions: Array<{
    questionId: number
    answer: string
  }>
}

export interface VerifySecurityQuestionsRequest {
  answers: Array<{
    questionId: number
    answer: string
  }>
}

// Security API Service
export const securityApi = {
  // Transaction PIN management
  createTransactionPin: async (request: CreatePinRequest): Promise<TransactionPin> => {
    const response = await api.post('/security/transaction-pin', request)
    return response.data
  },

  verifyTransactionPin: async (request: VerifyPinRequest): Promise<{ valid: boolean }> => {
    const response = await api.post('/security/transaction-pin/verify', request)
    return response.data
  },

  changeTransactionPin: async (request: ChangePinRequest): Promise<TransactionPin> => {
    const response = await api.put('/security/transaction-pin', request)
    return response.data
  },

  getTransactionPinStatus: async (): Promise<TransactionPin> => {
    const response = await api.get('/security/transaction-pin/status')
    return response.data
  },

  // Security Questions management
  getAvailableSecurityQuestions: async (): Promise<SecurityQuestion[]> => {
    const response = await api.get('/security/questions')
    return response.data
  },

  setSecurityQuestions: async (
    request: SetSecurityQuestionsRequest
  ): Promise<UserSecurityQuestion[]> => {
    const response = await api.post('/security/questions/user', request)
    return response.data
  },

  getUserSecurityQuestions: async (): Promise<UserSecurityQuestion[]> => {
    const response = await api.get('/security/questions/user')
    return response.data
  },

  verifySecurityQuestions: async (
    request: VerifySecurityQuestionsRequest
  ): Promise<{ valid: boolean }> => {
    const response = await api.post('/security/questions/verify', request)
    return response.data
  },

  updateSecurityQuestions: async (
    request: SetSecurityQuestionsRequest
  ): Promise<UserSecurityQuestion[]> => {
    const response = await api.put('/security/questions/user', request)
    return response.data
  },

  // Account recovery
  initiateAccountRecovery: async (email: string): Promise<{ message: string }> => {
    const response = await api.post('/security/recovery/initiate', { email })
    return response.data
  },

  verifyRecoveryCode: async (
    email: string,
    code: string
  ): Promise<{ valid: boolean; token?: string }> => {
    const response = await api.post('/security/recovery/verify', { email, code })
    return response.data
  },

  resetPasswordWithRecovery: async (
    token: string,
    newPassword: string
  ): Promise<{ message: string }> => {
    const response = await api.post('/security/recovery/reset-password', { token, newPassword })
    return response.data
  },
}

// Default security questions
export const DEFAULT_SECURITY_QUESTIONS: SecurityQuestion[] = [
  { id: 1, question: 'What was the name of your first pet?', category: 'personal' },
  { id: 2, question: "What is your mother's maiden name?", category: 'family' },
  { id: 3, question: 'What was the name of your first school?', category: 'education' },
  { id: 4, question: 'In what city were you born?', category: 'location' },
  { id: 5, question: 'What is the name of your favorite teacher?', category: 'education' },
  { id: 6, question: 'What was your childhood nickname?', category: 'personal' },
  { id: 7, question: 'What is the name of the street you grew up on?', category: 'location' },
  { id: 8, question: 'What was the make of your first car?', category: 'personal' },
  { id: 9, question: 'What is your favorite book?', category: 'interests' },
  { id: 10, question: 'What was the name of your first employer?', category: 'career' },
]
