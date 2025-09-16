import React from 'react'
import ReactDOM from 'react-dom/client'
import { RouterProvider } from 'react-router-dom'
import { router } from './router'
import { DarkModeProvider } from './store/darkMode'
import { GlobalToast } from './components/ui/GlobalToast'
import './styles.css'

const rootElement = document.getElementById('root')
if (!rootElement) throw new Error('Root element not found')

ReactDOM.createRoot(rootElement).render(
  <React.StrictMode>
    <DarkModeProvider>
      <RouterProvider router={router} />
      <GlobalToast />
    </DarkModeProvider>
  </React.StrictMode>
)
