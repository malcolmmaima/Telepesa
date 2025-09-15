import { useState, useEffect } from 'react'
import { Card } from './ui/Card'
import { Button } from './ui/Button'
import { formatCurrency } from '../lib/utils'
import { apiService } from '../api/client'

interface Payment {
  id: string
  biller: string
  account: string
  amount: number
  date: string
  status: string
}

export function RecentPayments() {
  const [payments, setPayments] = useState<Payment[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    loadRecentPayments()
  }, [])

  const loadRecentPayments = async () => {
    try {
      setLoading(true)
      setError(null)

      // Since the payments/recent endpoint doesn't exist yet, show empty state
      setPayments([])
    } catch (err: any) {
      console.error('Failed to load recent payments:', err)
      setError(err.message || 'Failed to load recent payments')
    } finally {
      setLoading(false)
    }
  }

  if (loading) {
    return (
      <Card title="Recent Payments" description="Your recent bill payments">
        <div className="flex justify-center items-center py-8">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-financial-blue"></div>
          <span className="ml-3 text-financial-gray">Loading payments...</span>
        </div>
      </Card>
    )
  }

  if (error) {
    return (
      <Card title="Recent Payments" description="Your recent bill payments">
        <div className="text-center py-8">
          <p className="text-red-600 mb-4">⚠️ {error}</p>
          <Button onClick={loadRecentPayments} variant="outline">
            Retry
          </Button>
        </div>
      </Card>
    )
  }

  if (payments.length === 0) {
    return (
      <Card title="Recent Payments" description="Your recent bill payments">
        <div className="text-center py-8">
          <p className="text-financial-gray mb-4">No recent payments found.</p>
          <p className="text-sm text-financial-gray">
            Your bill payments will appear here once you make them.
          </p>
        </div>
      </Card>
    )
  }

  return (
    <Card title="Recent Payments" description="Your recent bill payments">
      <div className="space-y-3">
        {payments.map(payment => (
          <div
            key={payment.id}
            className="flex items-center justify-between p-3 bg-gray-50 rounded-financial"
          >
            <div className="flex-1">
              <h4 className="font-medium text-financial-navy">{payment.biller}</h4>
              <p className="text-sm text-financial-gray">Account: {payment.account}</p>
            </div>
            <div className="text-right">
              <p className="font-semibold text-financial-navy">{formatCurrency(payment.amount)}</p>
              <p className="text-xs text-green-600">{payment.status}</p>
            </div>
            <div className="text-right ml-4">
              <p className="text-sm text-financial-gray">{payment.date}</p>
            </div>
          </div>
        ))}
        <Button variant="outline" className="w-full">
          View All Payments
        </Button>
      </div>
    </Card>
  )
}
