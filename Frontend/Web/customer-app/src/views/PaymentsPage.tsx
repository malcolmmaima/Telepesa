import { useState, useEffect } from 'react'
import { Card } from '../components/ui/Card'
import { Button } from '../components/ui/Button'
import { Input } from '../components/ui/Input'
import { formatCurrency } from '../lib/utils'
import { BillerDto, getPopularBillers, getBillerCategories, getBillers } from '../api/billers'

export function PaymentsPage() {
  const [selectedBiller, setSelectedBiller] = useState<BillerDto | null>(null)
  const [accountNumber, setAccountNumber] = useState('')
  const [amount, setAmount] = useState('')
  const [loading, setLoading] = useState(false)
  const [step, setStep] = useState<'select' | 'details' | 'confirm'>('select')
  const [billDetails, setBillDetails] = useState<any>(null)
  const [searchQuery, setSearchQuery] = useState('')
  
  // API state
  const [billers, setBillers] = useState<BillerDto[]>([])
  const [categories, setCategories] = useState<string[]>([])
  const [billersLoading, setBillersLoading] = useState(true)
  const [billersError, setBillersError] = useState<string | null>(null)

  // Load billers and categories on component mount
  useEffect(() => {
    loadBillersData()
  }, [])

  const loadBillersData = async () => {
    try {
      setBillersLoading(true)
      setBillersError(null)
      
      const [billersData, categoriesData] = await Promise.all([
        getPopularBillers(12), // Load more billers for better selection
        getBillerCategories(),
      ])
      
      setBillers(billersData)
      setCategories(categoriesData)
    } catch (error: any) {
      console.error('Failed to load billers:', error)
      setBillersError(error.message || 'Failed to load billers')
      // Fallback to empty arrays
      setBillers([])
      setCategories([])
    } finally {
      setBillersLoading(false)
    }
  }

  const filteredBillers = billers.filter(
    biller =>
      biller.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
      biller.category.toLowerCase().includes(searchQuery.toLowerCase())
  )

  const handleBillerSelect = (biller: BillerDto) => {
    setSelectedBiller(biller)
    setStep('details')
  }

  const handleFetchBillDetails = async () => {
    if (!selectedBiller || !accountNumber) return

    setLoading(true)
    try {
      // TODO: Implement API call to fetch bill details
      // const response = await fetchBillDetails(selectedBiller.id, accountNumber)

      // Mock bill details for demo
      setBillDetails({
        accountName: 'John Doe',
        outstandingAmount: 2500,
        dueDate: '2025-01-15',
        accountStatus: 'Active',
      })
      setStep('confirm')
    } catch (error: any) {
      alert(error.message || 'Failed to fetch bill details')
    } finally {
      setLoading(false)
    }
  }

  const handlePayment = async () => {
    if (!selectedBiller || !accountNumber || !amount) return

    setLoading(true)
    try {
      // TODO: Implement payment API call
      // await processBillPayment({
      //   billerId: selectedBiller.id,
      //   accountNumber,
      //   amount: parseFloat(amount)
      // })

      alert('Payment successful!')
      // Reset form
      setSelectedBiller(null)
      setAccountNumber('')
      setAmount('')
      setBillDetails(null)
      setStep('select')
    } catch (error: any) {
      alert(error.message || 'Payment failed')
    } finally {
      setLoading(false)
    }
  }

  if (step === 'details' && selectedBiller) {
    return (
      <div className="max-w-2xl mx-auto p-6 space-y-6">
        <div className="flex items-center space-x-4 mb-6">
          <Button variant="ghost" onClick={() => setStep('select')} className="p-2">
            ← Back
          </Button>
          <div>
            <h1 className="text-2xl font-bold text-financial-navy">Pay {selectedBiller.name}</h1>
            <p className="text-financial-gray">Enter your account details</p>
          </div>
        </div>

        <Card>
          <div className="space-y-4">
            <div className="flex items-center space-x-3 p-3 bg-gray-50 rounded-financial">
              <span className="text-2xl">{selectedBiller.logo}</span>
              <div>
                <h3 className="font-medium text-financial-navy">{selectedBiller.name}</h3>
                <p className="text-sm text-financial-gray">{selectedBiller.category}</p>
              </div>
            </div>

            <Input
              label={selectedBiller.accountLabel || 'Account Number'}
              placeholder={selectedBiller.accountPlaceholder}
              value={accountNumber}
              onChange={e => setAccountNumber(e.target.value)}
              required
            />

            <Button
              onClick={handleFetchBillDetails}
              disabled={!accountNumber}
              loading={loading}
              className="w-full"
            >
              Fetch Bill Details
            </Button>
          </div>
        </Card>
      </div>
    )
  }

  if (step === 'confirm' && selectedBiller && billDetails) {
    return (
      <div className="max-w-2xl mx-auto p-6 space-y-6">
        <div className="flex items-center space-x-4 mb-6">
          <Button variant="ghost" onClick={() => setStep('details')} className="p-2">
            ← Back
          </Button>
          <div>
            <h1 className="text-2xl font-bold text-financial-navy">Confirm Payment</h1>
            <p className="text-financial-gray">Review and confirm your payment</p>
          </div>
        </div>

        <Card>
          <div className="space-y-4">
            <div className="flex items-center space-x-3 p-3 bg-gray-50 rounded-financial">
              <span className="text-2xl">{selectedBiller.logo}</span>
              <div>
                <h3 className="font-medium text-financial-navy">{selectedBiller.name}</h3>
                <p className="text-sm text-financial-gray">Account: {accountNumber}</p>
              </div>
            </div>

            <div className="grid grid-cols-2 gap-4 p-4 bg-blue-50 rounded-financial">
              <div>
                <p className="text-sm text-financial-gray">Account Name</p>
                <p className="font-medium text-financial-navy">{billDetails.accountName}</p>
              </div>
              <div>
                <p className="text-sm text-financial-gray">Due Date</p>
                <p className="font-medium text-financial-navy">{billDetails.dueDate}</p>
              </div>
              <div>
                <p className="text-sm text-financial-gray">Outstanding Amount</p>
                <p className="font-medium text-financial-navy">
                  {formatCurrency(billDetails.outstandingAmount)}
                </p>
              </div>
              <div>
                <p className="text-sm text-financial-gray">Status</p>
                <p className="font-medium text-green-600">{billDetails.accountStatus}</p>
              </div>
            </div>

            <Input
              label="Amount to Pay"
              type="number"
              step="0.01"
              value={amount}
              onChange={e => setAmount(e.target.value)}
              placeholder={`Min: KSh 1, Outstanding: ${formatCurrency(billDetails.outstandingAmount)}`}
              required
            />

            <div className="flex space-x-3">
              <Button
                onClick={() => setAmount(billDetails.outstandingAmount.toString())}
                variant="outline"
                className="flex-1"
              >
                Pay Full Amount
              </Button>
              <Button
                onClick={handlePayment}
                disabled={!amount || parseFloat(amount) <= 0}
                loading={loading}
                className="flex-1"
              >
                Pay {amount ? formatCurrency(parseFloat(amount)) : ''}
              </Button>
            </div>
          </div>
        </Card>
      </div>
    )
  }

  return (
    <div className="max-w-6xl mx-auto p-6 space-y-8">
      {/* Header */}
      <div>
        <h1 className="text-3xl font-bold text-financial-navy mb-2">Pay Bills</h1>
        <p className="text-financial-gray">
          Pay your utility, telecom, and entertainment bills quickly and securely.
        </p>
      </div>

      {/* Search */}
      <Card title="Find Your Biller">
        <Input
          placeholder="Search for utilities, telecom, entertainment..."
          value={searchQuery}
          onChange={e => setSearchQuery(e.target.value)}
        />
      </Card>

      {/* Categories */}
      {billersLoading ? (
        <Card>
          <div className="flex justify-center items-center py-8">
            <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-financial-blue"></div>
            <span className="ml-3 text-financial-gray">Loading billers...</span>
          </div>
        </Card>
      ) : billersError ? (
        <Card>
          <div className="text-center py-8">
            <p className="text-red-600 mb-4">⚠️ {billersError}</p>
            <Button onClick={loadBillersData} variant="outline">
              Retry
            </Button>
          </div>
        </Card>
      ) : (
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
          {categories.map(category => (
            <Card key={category} className="cursor-pointer hover:shadow-lg transition-shadow">
              <div className="text-center p-4">
                <div className="text-2xl mb-2">
                  {category === 'Utilities' && '⚡'}
                  {category === 'Telecom' && '📱'}
                  {category === 'Entertainment' && '📺'}
                  {category === 'Internet' && '🌐'}
                  {category === 'Insurance' && '🏥'}
                  {category === 'Government' && '🏛️'}
                </div>
                <h3 className="font-medium text-financial-navy">{category}</h3>
                <p className="text-sm text-financial-gray">
                  {billers.filter(b => b.category === category).length} billers
                </p>
              </div>
            </Card>
          ))}
        </div>
      )}

      {/* Popular Billers */}
      <Card title="Popular Billers" description="Most commonly used bill payment services">
        {billersLoading ? (
          <div className="flex justify-center items-center py-12">
            <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-financial-blue"></div>
            <span className="ml-3 text-financial-gray">Loading billers...</span>
          </div>
        ) : billersError ? (
          <div className="text-center py-12">
            <p className="text-red-600 mb-4">⚠️ Failed to load billers</p>
            <p className="text-financial-gray mb-4">Please check your connection and try again.</p>
            <Button onClick={loadBillersData} variant="outline">
              Retry
            </Button>
          </div>
        ) : filteredBillers.length === 0 ? (
          <div className="text-center py-12">
            <p className="text-financial-gray mb-4">No billers found matching your search.</p>
            <Button onClick={() => setSearchQuery('')} variant="outline">
              Clear Search
            </Button>
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {filteredBillers.map(biller => (
              <div
                key={biller.id}
                onClick={() => handleBillerSelect(biller)}
                className="p-4 border rounded-financial hover:shadow-md transition-all cursor-pointer hover:border-financial-blue"
              >
                <div className="flex items-center space-x-3">
                  <span className="text-2xl">{biller.logo}</span>
                  <div className="flex-1">
                    <h4 className="font-medium text-financial-navy">{biller.name}</h4>
                    <p className="text-sm text-financial-gray">{biller.category}</p>
                  </div>
                  <svg
                    className="w-5 h-5 text-financial-gray"
                    fill="none"
                    stroke="currentColor"
                    viewBox="0 0 24 24"
                  >
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth={2}
                      d="m9 5 7 7-7 7"
                    />
                  </svg>
                </div>
              </div>
            ))}
          </div>
        )}
      </Card>

      {/* Recent Payments */}
      <Card title="Recent Payments" description="Your recent bill payments">
        <div className="space-y-3">
          {[
            {
              biller: 'Kenya Power (KPLC)',
              account: '••••1234',
              amount: 1500,
              date: '2025-01-08',
              status: 'Completed',
            },
            {
              biller: 'Safaricom Postpaid',
              account: '••••5678',
              amount: 800,
              date: '2025-01-05',
              status: 'Completed',
            },
            {
              biller: 'Nairobi Water',
              account: '••••9012',
              amount: 650,
              date: '2025-01-03',
              status: 'Completed',
            },
          ].map((payment, index) => (
            <div
              key={index}
              className="flex items-center justify-between p-3 bg-gray-50 rounded-financial"
            >
              <div className="flex-1">
                <h4 className="font-medium text-financial-navy">{payment.biller}</h4>
                <p className="text-sm text-financial-gray">Account: {payment.account}</p>
              </div>
              <div className="text-right">
                <p className="font-semibold text-financial-navy">
                  {formatCurrency(payment.amount)}
                </p>
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
    </div>
  )
}
