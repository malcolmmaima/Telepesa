import React from 'react'
import { describe, it, expect, beforeEach } from 'vitest'
import { renderHook, act } from '@testing-library/react'
import { useDarkMode, DarkModeProvider } from '../darkMode'

describe('useDarkMode', () => {
  beforeEach(() => {
    // Clear localStorage before each test
    localStorage.clear()
    // Remove any existing dark class from document
    document.documentElement.classList.remove('dark')
  })

  it('initializes with system preference when no stored preference', () => {
    const { result } = renderHook(() => useDarkMode(), {
      wrapper: DarkModeProvider,
    })

    // Should initialize based on system preference or default to false
    expect(typeof result.current.darkMode).toBe('boolean')
    expect(typeof result.current.toggleDarkMode).toBe('function')
  })

  it('initializes with stored preference when available', () => {
    localStorage.setItem('darkMode', 'true')

    const { result } = renderHook(() => useDarkMode(), {
      wrapper: DarkModeProvider,
    })

    expect(result.current.darkMode).toBe(true)
    expect(document.documentElement.classList.contains('dark')).toBe(true)
  })

  it('toggles dark mode correctly', () => {
    const { result } = renderHook(() => useDarkMode(), {
      wrapper: DarkModeProvider,
    })

    const initialMode = result.current.darkMode

    act(() => {
      result.current.toggleDarkMode()
    })

    expect(result.current.darkMode).toBe(!initialMode)
    expect(localStorage.getItem('darkMode')).toBe(JSON.stringify(!initialMode))
  })

  it('applies dark class to document element when dark mode is enabled', () => {
    const { result } = renderHook(() => useDarkMode(), {
      wrapper: DarkModeProvider,
    })

    act(() => {
      result.current.setDarkMode(true)
    })

    expect(result.current.darkMode).toBe(true)
    expect(document.documentElement.classList.contains('dark')).toBe(true)
  })

  it('removes dark class from document element when dark mode is disabled', () => {
    // Start with dark mode enabled
    localStorage.setItem('darkMode', 'true')

    const { result } = renderHook(() => useDarkMode(), {
      wrapper: DarkModeProvider,
    })

    expect(document.documentElement.classList.contains('dark')).toBe(true)

    act(() => {
      result.current.setDarkMode(false)
    })

    expect(result.current.darkMode).toBe(false)
    expect(document.documentElement.classList.contains('dark')).toBe(false)
  })

  it('persists preference to localStorage', () => {
    const { result } = renderHook(() => useDarkMode(), {
      wrapper: DarkModeProvider,
    })

    act(() => {
      result.current.setDarkMode(true)
    })

    expect(localStorage.getItem('darkMode')).toBe(JSON.stringify(result.current.darkMode))
  })
})
