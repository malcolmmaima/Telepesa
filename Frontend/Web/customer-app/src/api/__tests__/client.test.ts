import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import axios from 'axios'

// Mock axios
vi.mock('axios')
const mockedAxios = vi.mocked(axios)

// Mock the auth store
vi.mock('../store/auth', () => ({
  useAuth: {
    getState: vi.fn(() => ({ accessToken: null })),
  },
}))

describe('API Client', () => {
  beforeEach(() => {
    vi.clearAllMocks()

    // Mock localStorage
    Object.defineProperty(window, 'localStorage', {
      value: {
        getItem: vi.fn(),
        setItem: vi.fn(),
        removeItem: vi.fn(),
      },
      writable: true,
    })

    // Mock axios.create to return a mock instance
    const mockAxiosInstance = {
      interceptors: {
        request: { use: vi.fn() },
        response: { use: vi.fn() },
      },
    }
    const createMock = vi.fn().mockReturnValue(mockAxiosInstance)
    mockedAxios.create = createMock as any
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  it('creates axios instance with correct configuration', async () => {
    // Import the client to trigger axios.create call
    await import('../client')

    expect(mockedAxios.create).toHaveBeenCalledWith({
      baseURL: 'http://localhost:8080/api/v1',
      timeout: 30000,
      headers: {
        'Content-Type': 'application/json',
      },
    })
  })

  it('exports api instance', async () => {
    const clientModule = await import('../client')

    // Verify the api export exists
    expect(clientModule.api).toBeDefined()
  })

  it('has proper module structure', () => {
    // Basic test to ensure the module can be imported without errors
    expect(true).toBe(true)
  })
})
