import axios from 'axios'
import { useAuth } from '../store/auth'

export const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE || 'http://localhost:8080/api/v1',
})

api.interceptors.request.use((config) => {
  const { accessToken } = useAuth.getState()
  if (accessToken) {
    config.headers = config.headers || {}
    config.headers.Authorization = `Bearer ${accessToken}`
  }
  return config
})

let refreshing = false
let queue: Array<() => void> = []

api.interceptors.response.use(
  (r) => r,
  async (error) => {
    const original = error.config
    if (error.response?.status === 401 && !original._retry) {
      original._retry = true
      if (refreshing) {
        await new Promise<void>((res) => queue.push(res))
      }
      try {
        refreshing = true
        const { refreshToken, setSession, clear } = useAuth.getState()
        if (!refreshToken) {
          clear(); throw error
        }
        const res = await axios.post(`${api.defaults.baseURL?.replace(/\/api\/v1$/, '')}/api/v1/users/refresh`, { refreshToken })
        setSession({ accessToken: res.data.accessToken, refreshToken: res.data.refreshToken, user: useAuth.getState().user! })
        queue.forEach((fn) => fn()); queue = []
        return api(original)
      } catch (e) {
        useAuth.getState().clear()
        throw e
      } finally {
        refreshing = false
      }
    }
    throw error
  }
)


