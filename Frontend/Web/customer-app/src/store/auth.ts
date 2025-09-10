import { create } from 'zustand'

type User = {
  id: number
  username: string
  email: string
}

type AuthState = {
  accessToken: string | null
  refreshToken: string | null
  user: User | null
  setSession: (s: { accessToken: string, refreshToken: string, user: User }) => void
  clear: () => void
}

export const useAuth = create<AuthState>((set) => ({
  accessToken: null,
  refreshToken: null,
  user: null,
  setSession: (s) => set({ accessToken: s.accessToken, refreshToken: s.refreshToken, user: s.user }),
  clear: () => set({ accessToken: null, refreshToken: null, user: null }),
}))


