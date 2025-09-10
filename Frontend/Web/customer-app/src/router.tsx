import { createBrowserRouter } from 'react-router-dom'
import { LoginPage } from './views/LoginPage'
import { RegisterPage } from './views/RegisterPage'
import { ForgotPasswordPage } from './views/ForgotPasswordPage'
import { ProtectedLayout } from './views/ProtectedLayout'
import { HomePage } from './views/HomePage'
import { AccountsPage } from './views/AccountsPage'
import { TransactionsPage } from './views/TransactionsPage'
import { TransferPage } from './views/TransferPage'
import { ProfilePage } from './views/ProfilePage'
import { PaymentsPage } from './views/PaymentsPage'
import { LoansPage } from './views/LoansPage'
import { SecurityPage } from './views/SecurityPage'
import { SupportPage } from './views/SupportPage'
import { NotFoundPage } from './views/NotFoundPage'

export const router = createBrowserRouter([
  {
    path: '/',
    element: <ProtectedLayout />,
    children: [
      { index: true, element: <HomePage /> },
      { path: 'accounts', element: <AccountsPage /> },
      { path: 'transactions', element: <TransactionsPage /> },
      { path: 'transfers', element: <TransferPage /> },
      { path: 'payments', element: <PaymentsPage /> },
      { path: 'loans', element: <LoansPage /> },
      { path: 'profile', element: <ProfilePage /> },
      { path: 'security', element: <SecurityPage /> },
      { path: 'support', element: <SupportPage /> },
    ],
  },
  { path: '/login', element: <LoginPage /> },
  { path: '/register', element: <RegisterPage /> },
  { path: '/forgot-password', element: <ForgotPasswordPage /> },
  { path: '*', element: <NotFoundPage /> },
])
