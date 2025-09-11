import { apiClient } from './client'

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

const BASE = '/bill-payment-service/api/v1/billers'

export async function getBillers(params?: { category?: string; billType?: string; search?: string }) {
  const res = await apiClient.get<BillerDto[]>(BASE, { params })
  return res.data
}

export async function getBillerById(billerId: string) {
  const res = await apiClient.get<BillerDto>(`${BASE}/${billerId}`)
  return res.data
}

export async function getBillerCategories() {
  const res = await apiClient.get<string[]>(`${BASE}/categories`)
  return res.data
}

export async function getPopularBillers(limit = 8) {
  const res = await apiClient.get<BillerDto[]>(`${BASE}/popular`, { params: { limit } })
  return res.data
}

