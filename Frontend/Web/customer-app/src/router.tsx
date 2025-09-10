import { createBrowserRouter } from 'react-router-dom'
import { LoginPage } from './views/LoginPage'
import { RegisterPage } from './views/RegisterPage'
import { ForgotPasswordPage } from './views/ForgotPasswordPage'
import { ProtectedLayout } from './views/ProtectedLayout'
import { HomePage } from './views/HomePage'
import { AccountsPage } from './views/AccountsPage'
import { TransactionsPage } from './views/TransactionsPage'

export const router = createBrowserRouter([
  { path: '/', element: <ProtectedLayout />, children: [
    { index: true, element: <HomePage /> },
    { path: 'accounts', element: <AccountsPage /> },
    { path: 'transactions', element: <TransactionsPage /> },
  ]},
  { path: '/login', element: <LoginPage /> },
  { path: '/register', element: <RegisterPage /> },
  { path: '/forgot-password', element: <ForgotPasswordPage /> },
])


