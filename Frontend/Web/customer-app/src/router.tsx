import { createBrowserRouter } from 'react-router-dom'
import { LoginPage } from './views/LoginPage'
import { ProtectedLayout } from './views/ProtectedLayout'
import { HomePage } from './views/HomePage'

export const router = createBrowserRouter([
  { path: '/', element: <ProtectedLayout />, children: [
    { index: true, element: <HomePage /> },
  ]},
  { path: '/login', element: <LoginPage /> },
])


