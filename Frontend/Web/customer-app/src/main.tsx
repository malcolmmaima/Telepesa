import React from 'react'
import ReactDOM from 'react-dom/client'
import { RouterProvider } from 'react-router-dom'
import { router } from './router'
import { DarkModeProvider } from './store/darkMode'
import './styles.css'

const rootElement = document.getElementById('root')
if (!rootElement) throw new Error('Root element not found')

ReactDOM.createRoot(rootElement).render(
  <React.StrictMode>
    <DarkModeProvider>
      <RouterProvider router={router} />
    </DarkModeProvider>
  </React.StrictMode>
)
