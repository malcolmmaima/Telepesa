import { Card } from '../components/ui/Card'
import { Button } from '../components/ui/Button'

export function LoansPage() {
  return (
    <div className="max-w-6xl mx-auto p-6 space-y-8">
      <div>
        <h1 className="text-3xl font-bold text-financial-navy mb-2">Loans</h1>
        <p className="text-financial-gray">Apply for loans and track your loan status.</p>
      </div>

      <Card title="Available Loan Products" description="Choose a loan product that fits your needs">
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          {[{name:'Personal Loan', rate:'14%', max:'KSh 500,000'}, {name:'Business Loan', rate:'16%', max:'KSh 2,000,000'}, {name:'Emergency Loan', rate:'12%', max:'KSh 50,000'}].map((p, i) => (
            <div key={i} className="p-4 border rounded-financial">
              <h3 className="font-medium text-financial-navy">{p.name}</h3>
              <p className="text-sm text-financial-gray">Interest Rate: {p.rate}</p>
              <p className="text-sm text-financial-gray">Max Amount: {p.max}</p>
              <Button className="mt-3 w-full" variant="outline">Apply</Button>
            </div>
          ))}
        </div>
      </Card>

      <Card title="Your Loan Applications" description="Recent applications and their status">
        <div className="space-y-3">
          {[{product:'Personal Loan', amount:'KSh 150,000', status:'Under Review', date:'2025-01-07'}].map((l, i) => (
            <div key={i} className="p-3 bg-gray-50 rounded-financial flex justify-between">
              <div>
                <h4 className="font-medium text-financial-navy">{l.product}</h4>
                <p className="text-sm text-financial-gray">{l.date}</p>
              </div>
              <div className="text-right">
                <p className="font-semibold text-financial-navy">{l.amount}</p>
                <p className="text-xs text-yellow-600">{l.status}</p>
              </div>
            </div>
          ))}
        </div>
      </Card>
    </div>
  )
}

