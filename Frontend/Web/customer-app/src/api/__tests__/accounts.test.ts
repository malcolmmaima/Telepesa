import { describe, it, expect, vi, beforeEach } from 'vitest'
import { accountsApi } from '../accounts'

// Mock the entire client module
vi.mock('../client', () => ({
  api: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    delete: vi.fn(),
  },
}))

describe('Accounts API', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('getUserAccounts', () => {
    it('fetches user accounts successfully', async () => {
      const mockAccounts = [
        {
          id: 1,
          accountNumber: 'ACC001',
          userId: 1,
          accountType: 'SAVINGS' as const,
          accountName: 'My Savings',
          balance: 5000.0,
          availableBalance: 5000.0,
          minimumBalance: 0,
          status: 'ACTIVE' as const,
          currencyCode: 'KES',
          createdAt: '2023-01-01T00:00:00Z',
          updatedAt: '2023-01-01T00:00:00Z',
        },
      ]

      const { api } = await import('../client')
      vi.mocked(api.get).mockResolvedValue({
        data: { data: mockAccounts },
      })

      const result = await accountsApi.getUserAccounts(1)

      expect(api.get).toHaveBeenCalledWith('/accounts/user/1', {
        params: { page: 0, size: 20, sortBy: 'createdAt', sortDir: 'desc' },
      })
      expect(result).toEqual(mockAccounts)
    })

    it('handles empty response', async () => {
      const { api } = await import('../client')
      vi.mocked(api.get).mockResolvedValue({
        data: {},
      })

      const result = await accountsApi.getUserAccounts(1)
      expect(result).toEqual([])
    })
  })

  describe('getAccount', () => {
    it('fetches account by ID', async () => {
      const mockAccount = {
        id: 1,
        accountNumber: 'ACC001',
        userId: 1,
        accountType: 'SAVINGS' as const,
        accountName: 'My Savings',
        balance: 5000.0,
        availableBalance: 5000.0,
        minimumBalance: 0,
        status: 'ACTIVE' as const,
        currencyCode: 'KES',
        createdAt: '2023-01-01T00:00:00Z',
        updatedAt: '2023-01-01T00:00:00Z',
      }

      const { api } = await import('../client')
      vi.mocked(api.get).mockResolvedValue({
        data: { data: mockAccount },
      })

      const result = await accountsApi.getAccount(1)

      expect(api.get).toHaveBeenCalledWith('/accounts/1')
      expect(result).toEqual(mockAccount)
    })
  })

  describe('updateAccount', () => {
    it('updates account successfully', async () => {
      const mockAccount = {
        id: 1,
        accountNumber: 'ACC001',
        userId: 1,
        accountType: 'SAVINGS' as const,
        accountName: 'Emergency Fund',
        balance: 5000.0,
        availableBalance: 5000.0,
        minimumBalance: 0,
        status: 'ACTIVE' as const,
        currencyCode: 'KES',
        createdAt: '2023-01-01T00:00:00Z',
        updatedAt: '2023-01-01T00:00:00Z',
      }

      const { api } = await import('../client')
      vi.mocked(api.put).mockResolvedValue({
        data: { data: mockAccount },
      })

      const updates = { accountName: 'Emergency Fund' }
      const result = await accountsApi.updateAccount(1, updates)

      expect(api.put).toHaveBeenCalledWith('/accounts/1', updates)
      expect(result).toEqual(mockAccount)
    })
  })

  describe('getAccountBalance', () => {
    it('fetches account balance', async () => {
      const mockBalance = {
        accountId: 1,
        accountNumber: 'ACC001',
        balance: 5000.0,
        availableBalance: 5000.0,
        currencyCode: 'KES',
        lastUpdated: '2023-01-01T00:00:00Z',
      }

      const { api } = await import('../client')
      vi.mocked(api.get).mockResolvedValue({
        data: { data: mockBalance },
      })

      const result = await accountsApi.getAccountBalance(1)

      expect(api.get).toHaveBeenCalledWith('/accounts/1/balance')
      expect(result).toEqual(mockBalance)
    })
  })
})
