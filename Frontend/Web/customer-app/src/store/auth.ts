import { create } from 'zustand'
import { persist } from 'zustand/middleware'
import type { User, AuthState, LoginResponse } from '../types'

interface AuthActions {
  setSession: (session: LoginResponse) => void
  updateUser: (user: Partial<User>) => void
  logout: () => void
  clearError: () => void
  setLoading: (loading: boolean) => void
  setError: (error: string) => void
}

type AuthStore = AuthState & AuthActions

export const useAuth = create<AuthStore>()(
  persist(
    (set, get) => ({
      // State
      accessToken: null,
      refreshToken: null,
      user: null,
      isAuthenticated: false,
      isLoading: false,
      error: null,

      // Actions
      setSession: (session: LoginResponse) => {
        set({
          accessToken: session.accessToken,
          refreshToken: session.refreshToken,
          user: session.user,
          isAuthenticated: true,
          error: null,
          isLoading: false,
        })
      },

      updateUser: (userUpdate: Partial<User>) => {
        const currentUser = get().user
        if (currentUser) {
          set({
            user: { ...currentUser, ...userUpdate },
          })
        }
      },

      logout: () => {
        set({
          accessToken: null,
          refreshToken: null,
          user: null,
          isAuthenticated: false,
          error: null,
          isLoading: false,
        })
      },

      clearError: () => {
        set({ error: null })
      },

      setLoading: (loading: boolean) => {
        set({ isLoading: loading })
      },

      setError: (error: string) => {
        set({ error, isLoading: false })
      },
    }),
    {
      name: 'telepesa-auth',
      partialize: state => ({
        accessToken: state.accessToken,
        refreshToken: state.refreshToken,
        user: state.user,
        isAuthenticated: state.isAuthenticated,
      }),
    }
  )
)

// Computed selectors
export const useAuthSelectors = () => {
  const auth = useAuth()

  return {
    isLoggedIn: auth.isAuthenticated && !!auth.accessToken,
    userName: auth.user ? `${auth.user.firstName || auth.user.username}` : null,
    userInitials: auth.user
      ? `${auth.user.firstName?.[0] || ''}${auth.user.lastName?.[0] || auth.user.username[0]}`
      : null,
    hasVerifiedKYC: auth.user?.kycStatus === 'verified',
    isAccountActive: auth.user?.accountStatus === 'active',
  }
}
