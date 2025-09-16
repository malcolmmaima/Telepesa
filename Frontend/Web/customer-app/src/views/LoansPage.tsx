import { useState, useEffect } from 'react'
import { useAuth } from '../store/auth'
import {
  loansApi,
  type Loan,
  type LoanProduct,
  type LoanApplication,
  type LoanCalculation,
} from '../api/loans'
import { accountsApi, type Account } from '../api/accounts'
import { Card } from '../components/ui/Card'
import { Button } from '../components/ui/Button'
import { Input } from '../components/ui/Input'
import { formatCurrency, cn } from '../lib/utils'

type TabType = 'products' | 'apply' | 'my-loans' | 'calculator'

const LOAN_TYPE_ICONS = {
  PERSONAL: '👤',
  BUSINESS: '🏢',
  EMERGENCY: '🚨',
  ASSET: '🏠',
}

const LOAN_STATUS_COLORS = {
  PENDING: 'text-yellow-600 bg-yellow-50 border-yellow-200',
  APPROVED: 'text-blue-600 bg-blue-50 border-blue-200',
  REJECTED: 'text-red-600 bg-red-50 border-red-200',
  ACTIVE: 'text-green-600 bg-green-50 border-green-200',
  COMPLETED: 'text-gray-600 bg-gray-50 border-gray-200',
  DEFAULTED: 'text-red-800 bg-red-100 border-red-300',
}

export function LoansPage() {
  const { user } = useAuth()
  const [activeTab, setActiveTab] = useState<TabType>('products')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  // Data states
  const [loanProducts, setLoanProducts] = useState<LoanProduct[]>([])
  const [userLoans, setUserLoans] = useState<Loan[]>([])
  const [accounts, setAccounts] = useState<Account[]>([])
  const [selectedProduct, setSelectedProduct] = useState<LoanProduct | null>(null)

  // Application form state
  const [application, setApplication] = useState<LoanApplication>({
    loanType: 'PERSONAL',
    requestedAmount: 0,
    termMonths: 12,
    purpose: '',
    monthlyIncome: 0,
    employmentStatus: '',
    employerName: '',
    accountId: 0,
  })

  // Calculator state
  const [calculatorForm, setCalculatorForm] = useState({
    amount: 100000,
    termMonths: 12,
    loanType: 'PERSONAL' as Loan['loanType'],
  })
  const [calculation, setCalculation] = useState<LoanCalculation | null>(null)

  const [showApplicationForm, setShowApplicationForm] = useState(false)
  const [applicationSuccess, setApplicationSuccess] = useState<Loan | null>(null)

  // Load initial data
  useEffect(() => {
    if (user?.id) {
      loadData()
    }
  }, [user?.id])

  // Load loans when tab changes
  useEffect(() => {
    if (user?.id && activeTab === 'my-loans') {
      loadUserLoans()
    }
  }, [user?.id, activeTab])

  const loadData = async () => {
    try {
      setLoading(true)
      const [productsData, accountsData] = await Promise.allSettled([
        loansApi.getLoanProducts(),
        accountsApi.getUserAccounts(user!.id),
      ])

      if (productsData.status === 'fulfilled') {
        setLoanProducts(productsData.value)
      } else {
        // Fallback to default products
        setLoanProducts(getDefaultLoanProducts())
        console.log('Loan products API not available, using defaults')
      }

      if (accountsData.status === 'fulfilled') {
        setAccounts(accountsData.value)
        if (accountsData.value.length > 0) {
          setApplication(prev => ({ ...prev, accountId: accountsData.value[0].id }))
        }
      } else {
        console.log('Accounts API not available yet')
      }
    } catch (err: any) {
      console.log('Some loan features not available yet')
    } finally {
      setLoading(false)
    }
  }

  const loadUserLoans = async () => {
    try {
      const response = await loansApi.getUserLoans(user!.id, 0, 50)
      setUserLoans(response.content)
    } catch (err: any) {
      console.log('User loans API not available yet')
      setUserLoans([])
    }
  }

  const calculateLoan = async () => {
    try {
      const calc = await loansApi.calculateLoan(
        calculatorForm.amount,
        calculatorForm.termMonths,
        calculatorForm.loanType
      )
      setCalculation(calc)
    } catch (err: any) {
      // Fallback calculation
      const interestRate = getDefaultInterestRate(calculatorForm.loanType)
      const monthlyRate = interestRate / 100 / 12
      const monthlyPayment =
        (calculatorForm.amount *
          monthlyRate *
          Math.pow(1 + monthlyRate, calculatorForm.termMonths)) /
        (Math.pow(1 + monthlyRate, calculatorForm.termMonths) - 1)

      setCalculation({
        requestedAmount: calculatorForm.amount,
        interestRate,
        termMonths: calculatorForm.termMonths,
        monthlyPayment,
        totalInterest: monthlyPayment * calculatorForm.termMonths - calculatorForm.amount,
        totalAmount: monthlyPayment * calculatorForm.termMonths,
        schedule: [],
      })
    }
  }

  const handleApplyForLoan = (product: LoanProduct) => {
    setSelectedProduct(product)
    setApplication(prev => ({
      ...prev,
      loanType: product.loanType,
      requestedAmount: product.minAmount,
    }))
    setShowApplicationForm(true)
    setActiveTab('apply') // Switch to apply tab to show the form
  }

  const submitApplication = async (e: React.FormEvent) => {
    e.preventDefault()
    try {
      setLoading(true)
      // Update application with correct user ID
      const applicationWithUserId = {
        ...application,
        accountId: user!.id, // Use user ID instead of account ID for now
      }
      const loan = await loansApi.applyForLoan(applicationWithUserId)
      setApplicationSuccess(loan)
      setShowApplicationForm(false)
      await loadUserLoans()
    } catch (err: any) {
      setError(err.message || 'Failed to submit loan application')
    } finally {
      setLoading(false)
    }
  }

  // Fallback data functions
  const getDefaultLoanProducts = (): LoanProduct[] => [
    {
      id: 1,
      name: 'Personal Loan',
      loanType: 'PERSONAL',
      minAmount: 10000,
      maxAmount: 500000,
      interestRate: null,
      minInterestRate: 12,
      maxInterestRate: 18,
      minTermMonths: 6,
      maxTermMonths: 36,
      processingFee: null,
      processingFeePercentage: null,
      requiresCollateral: null,
      maxLtvRatio: null,
      eligibilityCriteria: null,
      requiredDocuments: null,
      description: 'Quick personal loans for your immediate needs',
      requirements: ['Valid ID', 'Proof of income', 'Bank statements'],
      features: ['Quick approval', 'Flexible terms', 'No collateral required'],
      currency: 'KES',
      isActive: true,
      createdAt: null,
      updatedAt: null,
      effectiveRate: null,
      termRange: null,
      amountRange: null,
    },
    {
      id: 2,
      name: 'Business Loan',
      loanType: 'BUSINESS',
      minAmount: 50000,
      maxAmount: 2000000,
      interestRate: null,
      minInterestRate: 14,
      maxInterestRate: 20,
      minTermMonths: 12,
      maxTermMonths: 60,
      processingFee: null,
      processingFeePercentage: null,
      requiresCollateral: null,
      maxLtvRatio: null,
      eligibilityCriteria: null,
      requiredDocuments: null,
      description: 'Grow your business with our competitive business loans',
      requirements: ['Business registration', 'Financial statements', 'Business plan'],
      features: ['Higher amounts', 'Longer terms', 'Business support'],
      currency: 'KES',
      isActive: true,
      createdAt: null,
      updatedAt: null,
      effectiveRate: null,
      termRange: null,
      amountRange: null,
    },
    {
      id: 3,
      name: 'Emergency Loan',
      loanType: 'EMERGENCY',
      minAmount: 5000,
      maxAmount: 100000,
      interestRate: null,
      minInterestRate: 10,
      maxInterestRate: 15,
      minTermMonths: 3,
      maxTermMonths: 12,
      processingFee: null,
      processingFeePercentage: null,
      requiresCollateral: null,
      maxLtvRatio: null,
      eligibilityCriteria: null,
      requiredDocuments: null,
      description: 'Fast cash for unexpected expenses',
      requirements: ['Valid ID', 'Active account'],
      features: ['Same day approval', 'Quick disbursement', 'Lower rates'],
      currency: 'KES',
      isActive: true,
      createdAt: null,
      updatedAt: null,
      effectiveRate: null,
      termRange: null,
      amountRange: null,
    },
  ]

  const getDefaultInterestRate = (loanType: Loan['loanType']): number => {
    switch (loanType) {
      case 'EMERGENCY':
        return 12
      case 'PERSONAL':
        return 15
      case 'BUSINESS':
        return 18
      case 'ASSET':
        return 14
      default:
        return 15
    }
  }

  const tabs = [
    { id: 'products', label: 'Loan Products', icon: '💼' },
    { id: 'apply', label: 'Apply', icon: '📝' },
    { id: 'my-loans', label: 'My Loans', icon: '📊' },
    { id: 'calculator', label: 'Calculator', icon: '🧮' },
  ]

  return (
    <div className="max-w-7xl mx-auto p-6 space-y-8">
      {/* Header */}
      <div>
        <h1 className="text-3xl font-bold text-financial-navy mb-2">Loans 💰</h1>
        <p className="text-financial-gray">
          Apply for loans, manage existing loans, and calculate loan terms
        </p>
      </div>

      {/* Error Display */}
      {error && (
        <div className="p-4 bg-red-50 border border-red-200 rounded-financial">
          <p className="text-red-600 flex items-center">
            <span className="mr-2">⚠️</span>
            {error}
          </p>
          <Button
            variant="ghost"
            size="sm"
            onClick={() => setError(null)}
            className="mt-2 text-red-600"
          >
            Dismiss
          </Button>
        </div>
      )}

      {/* Success Message */}
      {applicationSuccess && (
        <div className="p-4 bg-green-50 border border-green-200 rounded-financial">
          <p className="text-green-600 flex items-center">
            <span className="mr-2">✅</span>
            Your loan application has been submitted successfully! Application ID: #
            {applicationSuccess.id}
          </p>
          <Button
            variant="ghost"
            size="sm"
            onClick={() => setApplicationSuccess(null)}
            className="mt-2 text-green-600"
          >
            Dismiss
          </Button>
        </div>
      )}

      {/* Tab Navigation */}
      <div className="flex space-x-1 p-1 bg-gray-100 rounded-financial">
        {tabs.map(tab => (
          <button
            key={tab.id}
            onClick={() => setActiveTab(tab.id as TabType)}
            className={cn(
              'flex-1 py-2 px-4 rounded-financial text-sm font-medium transition-all duration-200',
              activeTab === tab.id
                ? 'bg-white text-financial-navy shadow-sm'
                : 'text-financial-gray hover:text-financial-navy'
            )}
          >
            {tab.icon} {tab.label}
          </button>
        ))}
      </div>

      {/* Tab Content */}
      {activeTab === 'products' && (
        <Card
          title="Available Loan Products"
          description="Choose a loan product that fits your needs"
        >
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {loanProducts.map(product => (
              <div
                key={product.id}
                className="p-6 border rounded-financial-lg hover-lift cursor-pointer transition-all duration-200"
              >
                <div className="flex items-center mb-4">
                  <div className="text-3xl mr-3">{LOAN_TYPE_ICONS[product.loanType]}</div>
                  <div>
                    <h3 className="font-semibold text-financial-navy">{product.name}</h3>
                    <p className="text-sm text-financial-gray">{product.loanType}</p>
                  </div>
                </div>

                <div className="space-y-2 mb-4">
                  <div className="flex justify-between text-sm">
                    <span className="text-financial-gray">Amount:</span>
                    <span className="font-medium">
                      {formatCurrency(product.minAmount)} - {formatCurrency(product.maxAmount)}
                    </span>
                  </div>
                  <div className="flex justify-between text-sm">
                    <span className="text-financial-gray">Interest:</span>
                    <span className="font-medium">
                      {product.minInterestRate}% - {product.maxInterestRate}%
                    </span>
                  </div>
                  <div className="flex justify-between text-sm">
                    <span className="text-financial-gray">Terms:</span>
                    <span className="font-medium">
                      {product.minTermMonths} - {product.maxTermMonths} months
                    </span>
                  </div>
                </div>

                <p className="text-sm text-financial-gray mb-4">{product.description}</p>

                <div className="space-y-2 mb-4">
                  <h4 className="text-sm font-medium text-financial-navy">Features:</h4>
                  <ul className="text-xs text-financial-gray space-y-1">
                    {product.features.map((feature, index) => (
                      <li key={index}>• {feature}</li>
                    ))}
                  </ul>
                </div>

                <Button
                  onClick={() => handleApplyForLoan(product)}
                  className="w-full"
                  variant="outline"
                >
                  Apply Now
                </Button>
              </div>
            ))}
          </div>
        </Card>
      )}

      {activeTab === 'apply' && (
        <Card title="Loan Application" description="Fill out the form to apply for a loan">
          {!showApplicationForm ? (
            <div className="text-center py-8">
              <div className="text-6xl mb-4">💰</div>
              <h3 className="text-xl font-semibold text-financial-navy mb-2">
                Ready to apply for a loan?
              </h3>
              <p className="text-financial-gray mb-6">
                Choose a loan product from our available options to get started.
              </p>
              <Button onClick={() => setActiveTab('products')}>View Loan Products</Button>
            </div>
          ) : (
            <form onSubmit={submitApplication} className="space-y-6">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <Input
                  label="Loan Type"
                  value={application.loanType}
                  disabled
                  className="bg-gray-50"
                />

                <Input
                  label="Requested Amount"
                  type="number"
                  value={application.requestedAmount}
                  onChange={e =>
                    setApplication({ ...application, requestedAmount: Number(e.target.value) })
                  }
                  min={selectedProduct?.minAmount}
                  max={selectedProduct?.maxAmount}
                  required
                />

                <Input
                  label="Loan Term (months)"
                  type="number"
                  value={application.termMonths}
                  onChange={e =>
                    setApplication({ ...application, termMonths: Number(e.target.value) })
                  }
                  min={selectedProduct?.minTermMonths}
                  max={selectedProduct?.maxTermMonths}
                  required
                />

                <Input
                  label="Monthly Income"
                  type="number"
                  value={application.monthlyIncome}
                  onChange={e =>
                    setApplication({ ...application, monthlyIncome: Number(e.target.value) })
                  }
                  required
                />

                <Input
                  label="Employment Status"
                  value={application.employmentStatus}
                  onChange={e =>
                    setApplication({ ...application, employmentStatus: e.target.value })
                  }
                  placeholder="e.g., Employed, Self-employed"
                  required
                />

                <Input
                  label="Employer Name (Optional)"
                  value={application.employerName || ''}
                  onChange={e => setApplication({ ...application, employerName: e.target.value })}
                  placeholder="Your employer's name"
                />
              </div>

              <Input
                label="Purpose of Loan"
                value={application.purpose}
                onChange={e => setApplication({ ...application, purpose: e.target.value })}
                placeholder="Brief description of how you'll use the loan"
                required
              />

              <div className="flex gap-4 pt-4">
                <Button
                  type="button"
                  variant="secondary"
                  onClick={() => setShowApplicationForm(false)}
                  className="flex-1"
                >
                  Cancel
                </Button>
                <Button type="submit" disabled={loading} className="flex-1">
                  {loading ? '⏳ Submitting...' : '🎉 Submit Application'}
                </Button>
              </div>
            </form>
          )}
        </Card>
      )}

      {activeTab === 'my-loans' && (
        <Card title="My Loans" description="Track your loan applications and active loans">
          {userLoans.length === 0 ? (
            <div className="text-center py-8">
              <div className="text-6xl mb-4">📊</div>
              <h3 className="text-xl font-semibold text-financial-navy mb-2">No loans yet</h3>
              <p className="text-financial-gray mb-6">
                You haven't applied for any loans yet. Ready to get started?
              </p>
              <Button onClick={() => setActiveTab('products')}>Browse Loan Products</Button>
            </div>
          ) : (
            <div className="space-y-4">
              {userLoans.map(loan => (
                <div
                  key={loan.id}
                  className="p-6 border rounded-financial-lg hover-lift transition-all duration-200"
                >
                  <div className="flex justify-between items-start mb-4">
                    <div className="flex items-center">
                      <div className="text-2xl mr-3">{LOAN_TYPE_ICONS[loan.loanType]}</div>
                      <div>
                        <h3 className="font-semibold text-financial-navy">
                          {loan.loanType} Loan #{loan.id}
                        </h3>
                        <p className="text-sm text-financial-gray">{loan.purpose}</p>
                      </div>
                    </div>
                    <div
                      className={cn(
                        'px-3 py-1 rounded-full text-xs font-medium',
                        LOAN_STATUS_COLORS[loan.status]
                      )}
                    >
                      {loan.status}
                    </div>
                  </div>

                  <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                    <div>
                      <div className="text-sm text-financial-gray">Principal</div>
                      <div className="font-semibold">{formatCurrency(loan.principal)}</div>
                    </div>
                    <div>
                      <div className="text-sm text-financial-gray">Monthly Payment</div>
                      <div className="font-semibold">{formatCurrency(loan.monthlyPayment)}</div>
                    </div>
                    <div>
                      <div className="text-sm text-financial-gray">Interest Rate</div>
                      <div className="font-semibold">{loan.interestRate}%</div>
                    </div>
                    <div>
                      <div className="text-sm text-financial-gray">Remaining</div>
                      <div className="font-semibold">{formatCurrency(loan.remainingBalance)}</div>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </Card>
      )}

      {activeTab === 'calculator' && (
        <Card title="Loan Calculator" description="Calculate your monthly payments and total cost">
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
            {/* Calculator Form */}
            <div className="space-y-6">
              <div>
                <label className="block text-sm font-medium text-financial-navy mb-2">
                  Loan Type
                </label>
                <select
                  value={calculatorForm.loanType}
                  onChange={e =>
                    setCalculatorForm({
                      ...calculatorForm,
                      loanType: e.target.value as Loan['loanType'],
                    })
                  }
                  className="w-full input"
                >
                  <option value="PERSONAL">💼 Personal Loan</option>
                  <option value="BUSINESS">🏢 Business Loan</option>
                  <option value="EMERGENCY">🚨 Emergency Loan</option>
                  <option value="ASSET">🏠 Asset Loan</option>
                </select>
              </div>

              <Input
                label="Loan Amount"
                type="number"
                value={calculatorForm.amount}
                onChange={e =>
                  setCalculatorForm({ ...calculatorForm, amount: Number(e.target.value) })
                }
                min={10000}
                step={5000}
              />

              <Input
                label="Loan Term (months)"
                type="number"
                value={calculatorForm.termMonths}
                onChange={e =>
                  setCalculatorForm({ ...calculatorForm, termMonths: Number(e.target.value) })
                }
                min={3}
                max={60}
              />

              <Button onClick={calculateLoan} className="w-full">
                🧮 Calculate
              </Button>
            </div>

            {/* Calculation Results */}
            <div>
              {calculation ? (
                <div className="space-y-6">
                  <div className="p-6 bg-gradient-to-r from-financial-navy to-financial-blue text-white rounded-financial-lg">
                    <div className="text-sm opacity-90">Monthly Payment</div>
                    <div className="text-3xl font-bold">
                      {formatCurrency(calculation.monthlyPayment)}
                    </div>
                  </div>

                  <div className="grid grid-cols-2 gap-4">
                    <div className="p-4 bg-gray-50 rounded-financial">
                      <div className="text-sm text-financial-gray">Total Interest</div>
                      <div className="text-lg font-semibold text-financial-navy">
                        {formatCurrency(calculation.totalInterest)}
                      </div>
                    </div>
                    <div className="p-4 bg-gray-50 rounded-financial">
                      <div className="text-sm text-financial-gray">Total Amount</div>
                      <div className="text-lg font-semibold text-financial-navy">
                        {formatCurrency(calculation.totalAmount)}
                      </div>
                    </div>
                  </div>

                  <div className="p-4 bg-blue-50 rounded-financial">
                    <h4 className="font-medium text-financial-navy mb-2">Loan Summary</h4>
                    <ul className="text-sm text-financial-gray space-y-1">
                      <li>• Loan Amount: {formatCurrency(calculation.requestedAmount)}</li>
                      <li>• Interest Rate: {calculation.interestRate}% per annum</li>
                      <li>• Loan Term: {calculation.termMonths} months</li>
                      <li>• Monthly Payment: {formatCurrency(calculation.monthlyPayment)}</li>
                    </ul>
                  </div>
                </div>
              ) : (
                <div className="text-center py-12">
                  <div className="text-6xl mb-4">🧮</div>
                  <h3 className="text-xl font-semibold text-financial-navy mb-2">
                    Calculate Your Loan
                  </h3>
                  <p className="text-financial-gray">
                    Enter your loan details and click calculate to see the payment breakdown.
                  </p>
                </div>
              )}
            </div>
          </div>
        </Card>
      )}
    </div>
  )
}
