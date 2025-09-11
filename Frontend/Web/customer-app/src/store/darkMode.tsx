import React, { createContext, useContext, useEffect, useState, ReactNode } from 'react'

interface DarkModeContextType {
  darkMode: boolean
  toggleDarkMode: () => void
  setDarkMode: (value: boolean) => void
}

const DarkModeContext = createContext<DarkModeContextType | undefined>(undefined)

export function useDarkMode() {
  const context = useContext(DarkModeContext)
  if (context === undefined) {
    throw new Error('useDarkMode must be used within a DarkModeProvider')
  }
  return context
}

interface DarkModeProviderProps {
  children: ReactNode
}

export function DarkModeProvider({ children }: DarkModeProviderProps) {
  const [darkMode, setDarkModeState] = useState(() => {
    // Check local storage first
    const saved = localStorage.getItem('darkMode')
    if (saved !== null) {
      return JSON.parse(saved)
    }
    // Check system preference
    return window.matchMedia('(prefers-color-scheme: dark)').matches
  })

  const setDarkMode = (value: boolean) => {
    setDarkModeState(value)
    localStorage.setItem('darkMode', JSON.stringify(value))
    
    // Update document class for Tailwind dark mode
    if (value) {
      document.documentElement.classList.add('dark')
    } else {
      document.documentElement.classList.remove('dark')
    }
  }

  const toggleDarkMode = () => {
    setDarkMode(!darkMode)
  }

  // Apply dark mode class on initial load
  useEffect(() => {
    if (darkMode) {
      document.documentElement.classList.add('dark')
    } else {
      document.documentElement.classList.remove('dark')
    }
  }, [darkMode])

  // Listen for system theme changes
  useEffect(() => {
    const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)')
    const handleChange = (e: MediaQueryListEvent) => {
      // Only update if no manual preference is stored
      const saved = localStorage.getItem('darkMode')
      if (saved === null) {
        setDarkMode(e.matches)
      }
    }

    mediaQuery.addEventListener('change', handleChange)
    return () => mediaQuery.removeEventListener('change', handleChange)
  }, [])

  return (
    <DarkModeContext.Provider value={{ darkMode, toggleDarkMode, setDarkMode }}>
      {children}
    </DarkModeContext.Provider>
  )
}
