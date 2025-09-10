import { useAuth } from '../store/auth'
import { Card } from '../components/ui/Card'
import { Button } from '../components/ui/Button'
import { formatCurrency } from '../lib/utils'

export function HomePage() {
  const { user } = useAuth()
  
  // Placeholder demo data; will be wired to API later
  const totalBalance = 254320.75
  const accounts = [
    { id: 1, name: 'Savings Account', number: '1234567890', balance: 150000.0 },
    { id: 2, name: 'Current Account', number: '0987654321', balance: 74320.75 },
  ]
  const quickActions = [
    { id: 'transfer', label: 'Transfer', path: '/transfers' },
    { id: 'pay', label: 'Pay Bills', path: '/payments' },
    { id: 'loan', label: 'Apply Loan', path: '/loans' },
  ]

  return (
    <div className="max-w-7xl mx-auto p-6 space-y-6">
      {/* Hero / Summary */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <Card className="lg:col-span-2" title={`Welcome back${user ? `, ${user.firstName ?? user.username}` : ''}`} description="Here’s a quick snapshot of your finances.">
          <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
            <div className="p-4 rounded-financial bg-white border border-gray-100">
              <div className="text-sm text-financial-gray">Total Balance</div>
              <div className="text-2xl font-semibold text-financial-navy mt-1">{formatCurrency(totalBalance)}</div>
            </div>
            <div className="p-4 rounded-financial bg-white border border-gray-100">
              <div className="text-sm text-financial-gray">Active Accounts</div>
              <div className="text-2xl font-semibold text-financial-navy mt-1">{accounts.length}</div>
            </div>
            <div className="p-4 rounded-financial bg-white border border-gray-100">
              <div className="text-sm text-financial-gray">Active Loans</div>
              <div className="text-2xl font-semibold text-financial-navy mt-1">0</div>
            </div>
          </div>
        </Card>
        <Card title="Quick Actions">
          <div className="grid grid-cols-3 gap-3">
            {quickActions.map(a => (
              <Button key={a.id} variant="secondary" className="w-full" onClick={() => (window.location.href = a.path)}>
                {a.label}
              </Button>
            ))}
          </div>
        </Card>
      </div>

      {/* Accounts */}
      <Card title="Your Accounts" description="Balances and recent activity.">
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {accounts.map(acc => (
            <div key={acc.id} className="border rounded-financial p-4 bg-white">
              <div className="flex items-center justify-between">
                <div>
                  <div className="text-financial-navy font-medium">{acc.name}</div>
                  <div className="text-sm text-financial-gray">•••• {acc.number.slice(-4)}</div>
                </div>
                <div className="text-financial-navy font-semibold">{formatCurrency(acc.balance)}</div>
              </div>
              <div className="mt-4 flex items-center gap-2">
                <Button size="sm" variant="secondary">Transfer</Button>
                <Button size="sm" variant="ghost">View</Button>
              </div>
            </div>
          ))}
        </div>
      </Card>
    </div>
  )
}


