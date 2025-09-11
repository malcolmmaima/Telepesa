import { api } from './client'

export interface BillerDto {
  id: string
  name: string
  category: string
  logo: string
  requiresAccount: boolean
  accountLabel?: string
  accountPlaceholder?: string
  billType: string
  serviceProviderCode: string
  active: boolean
  description?: string
}

export async function getBillers(params?: { category?: string; billType?: string; search?: string }) {
  const response = await api.get('http://localhost:8080/bill-payment-service/api/v1/billers', { params })
  return Array.isArray(response.data?.data) ? response.data.data : (Array.isArray(response.data) ? response.data : [])
}

export async function getBillerById(billerId: string) {
  const response = await api.get(`http://localhost:8080/bill-payment-service/api/v1/billers/${billerId}`)
  return response.data?.data || response.data
}

export async function getBillerCategories() {
  const response = await api.get('http://localhost:8080/bill-payment-service/api/v1/billers/categories')
  return Array.isArray(response.data?.data) ? response.data.data : (Array.isArray(response.data) ? response.data : [])
}

export async function getPopularBillers(limit = 8) {
  const response = await api.get('http://localhost:8080/bill-payment-service/api/v1/billers/popular', { params: { limit } })
  return Array.isArray(response.data?.data) ? response.data.data : (Array.isArray(response.data) ? response.data : [])
}

