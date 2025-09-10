import { type ClassValue, clsx } from 'clsx'
import { twMerge } from 'tailwind-merge'

/**
 * Combines class names with clsx and merges Tailwind classes
 */
export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs))
}

/**
 * Format currency amounts
 */
export function formatCurrency(amount: number, currency = 'KES'): string {
  return new Intl.NumberFormat('en-KE', {
    style: 'currency',
    currency,
  }).format(amount)
}

/**
 * Format account numbers with masking
 */
export function formatAccountNumber(accountNumber: string, showLast = 4): string {
  if (accountNumber.length <= showLast) {
    return accountNumber
  }
  const masked = '*'.repeat(accountNumber.length - showLast)
  const visible = accountNumber.slice(-showLast)
  return masked + visible
}

/**
 * Format dates in a financial context
 */
export function formatDate(date: string | Date, format: 'short' | 'medium' | 'long' = 'medium'): string {
  const dateObj = typeof date === 'string' ? new Date(date) : date
  
  const options: Intl.DateTimeFormatOptions = {
    short: { month: 'short', day: 'numeric', year: '2-digit' },
    medium: { month: 'short', day: 'numeric', year: 'numeric' },
    long: { weekday: 'long', month: 'long', day: 'numeric', year: 'numeric' }
  }[format]
  
  return new Intl.DateTimeFormat('en-KE', options).format(dateObj)
}

/**
 * Validate Kenyan phone numbers
 */
export function validateKenyanPhone(phone: string): boolean {
  // Remove all non-digit characters
  const cleanPhone = phone.replace(/\D/g, '')
  
  // Check if it matches Kenyan phone number patterns
  const patterns = [
    /^254[17][0-9]{8}$/, // +254 format
    /^0[17][0-9]{8}$/,   // 0 format
    /^[17][0-9]{8}$/     // Without country code or 0
  ]
  
  return patterns.some(pattern => pattern.test(cleanPhone))
}

/**
 * Format Kenyan phone number
 */
export function formatKenyanPhone(phone: string): string {
  const cleanPhone = phone.replace(/\D/g, '')
  
  if (cleanPhone.startsWith('254')) {
    return `+${cleanPhone.slice(0, 3)} ${cleanPhone.slice(3, 6)} ${cleanPhone.slice(6)}`
  } else if (cleanPhone.startsWith('0')) {
    return `${cleanPhone.slice(0, 4)} ${cleanPhone.slice(4, 7)} ${cleanPhone.slice(7)}`
  } else if (cleanPhone.length === 9) {
    return `0${cleanPhone.slice(0, 3)} ${cleanPhone.slice(3, 6)} ${cleanPhone.slice(6)}`
  }
  
  return phone
}

/**
 * Generate transaction reference
 */
export function generateTransactionRef(prefix = 'TXN'): string {
  const timestamp = Date.now().toString(36)
  const random = Math.random().toString(36).substring(2, 8)
  return `${prefix}${timestamp}${random}`.toUpperCase()
}

/**
 * Debounce function for search inputs
 */
export function debounce<T extends (...args: any[]) => any>(
  func: T,
  wait: number
): (...args: Parameters<T>) => void {
  let timeout: NodeJS.Timeout
  return (...args: Parameters<T>) => {
    clearTimeout(timeout)
    timeout = setTimeout(() => func(...args), wait)
  }
}

/**
 * Check if amount is within limits
 */
export function validateAmount(amount: number, min = 1, max = 1000000): {
  isValid: boolean
  error?: string
} {
  if (amount < min) {
    return { isValid: false, error: `Minimum amount is ${formatCurrency(min)}` }
  }
  
  if (amount > max) {
    return { isValid: false, error: `Maximum amount is ${formatCurrency(max)}` }
  }
  
  return { isValid: true }
}

/**
 * Get transaction status color
 */
export function getTransactionStatusColor(status: string): string {
  const statusColors: Record<string, string> = {
    'completed': 'text-financial-success bg-financial-success/10',
    'pending': 'text-financial-warning bg-financial-warning/10',
    'failed': 'text-financial-danger bg-financial-danger/10',
    'cancelled': 'text-financial-gray bg-financial-gray/10',
  }
  
  return statusColors[status.toLowerCase()] || 'text-financial-gray bg-financial-gray/10'
}

/**
 * Truncate text with ellipsis
 */
export function truncate(text: string, maxLength: number): string {
  if (text.length <= maxLength) return text
  return text.substring(0, maxLength) + '...'
}

/**
 * Calculate loan installment
 */
export function calculateLoanInstallment(
  principal: number,
  annualRate: number,
  termMonths: number
): number {
  const monthlyRate = annualRate / 100 / 12
  if (monthlyRate === 0) return principal / termMonths
  
  const factor = Math.pow(1 + monthlyRate, termMonths)
  return (principal * monthlyRate * factor) / (factor - 1)
}
