import { create } from 'zustand'

export type ToastType = 'success' | 'error' | 'warning' | 'info'

export interface Toast {
  id?: string
  type: ToastType
  title: string
  message: string
  duration?: number
  actionText?: string
  actionUrl?: string
}

interface ToastState {
  current: Toast | null
  show: (toast: Toast) => void
  clear: () => void
  showSuccess: (title: string, message: string, duration?: number) => void
  showError: (title: string, message: string, duration?: number) => void
}

export const useToast = create<ToastState>((set) => ({
  current: null,
  show: (toast) => set({ current: { duration: 5000, ...toast, id: crypto.randomUUID() } }),
  clear: () => set({ current: null }),
  showSuccess: (title, message, duration) =>
    set({ current: { id: crypto.randomUUID(), type: 'success', title, message, duration: duration ?? 4000 } }),
  showError: (title, message, duration) =>
    set({ current: { id: crypto.randomUUID(), type: 'error', title, message, duration: duration ?? 6000 } }),
}))

// Convenience non-hook API for places outside React components (e.g., axios interceptors)
export const toast = {
  show: (t: Toast) => useToast.getState().show(t),
  success: (title: string, message: string, duration?: number) => useToast.getState().showSuccess(title, message, duration),
  error: (title: string, message: string, duration?: number) => useToast.getState().showError(title, message, duration),
  clear: () => useToast.getState().clear(),
}


