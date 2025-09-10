import { render, screen, fireEvent, waitFor } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { describe, it, expect, vi } from 'vitest'
import { LoginPage } from '../LoginPage'

// Mock the API
vi.mock('../../api/client', () => ({
  api: {
    post: vi.fn(),
  },
}))

// Mock the auth store
vi.mock('../../store/auth', () => ({
  useAuth: () => ({
    setSession: vi.fn(),
  }),
}))

// Mock Lottie component
vi.mock('lottie-react', () => ({
  default: ({ className }: { className: string }) => (
    <div className={className} data-testid="lottie-animation">
      Animation
    </div>
  ),
}))

const renderLoginPage = () => {
  return render(
    <MemoryRouter>
      <LoginPage />
    </MemoryRouter>
  )
}

describe('LoginPage', () => {
  it('renders the login form correctly', () => {
    renderLoginPage()

    // Check for main elements
    expect(screen.getByText('Telepesa')).toBeInTheDocument()
    expect(screen.getByText('Welcome back! 👋')).toBeInTheDocument()
    expect(
      screen.getByText("Ready to manage your finances? Let's get you signed in!")
    ).toBeInTheDocument()

    // Check for form fields
    expect(screen.getByLabelText('Username or Email')).toBeInTheDocument()
    expect(screen.getByLabelText('Password')).toBeInTheDocument()

    // Check for buttons and links
    expect(screen.getByRole('button', { name: '🔐 Sign In' })).toBeInTheDocument()
    expect(screen.getByText('🔑 Forgot your password?')).toBeInTheDocument()
    expect(screen.getByText('Create your account 🎉')).toBeInTheDocument()
  })

  it('displays validation errors for empty fields', async () => {
    renderLoginPage()

    const signInButton = screen.getByRole('button', { name: '🔐 Sign In' })
    fireEvent.click(signInButton)

    await waitFor(() => {
      expect(screen.getByText('Username or email is required')).toBeInTheDocument()
      expect(screen.getByText('Password is required')).toBeInTheDocument()
    })
  })

  it('shows split-screen layout on desktop', () => {
    renderLoginPage()

    // Check for animation container (hidden on mobile, visible on desktop)
    const animationContainer = screen.getByTestId('lottie-animation')
    expect(animationContainer).toBeInTheDocument()

    // Check for mobile logo (visible on mobile, hidden on desktop)
    const mobileLogo = screen.getByText('Your Money, Your Way')
    expect(mobileLogo).toBeInTheDocument()
  })

  it('displays correct links', () => {
    renderLoginPage()

    const forgotPasswordLink = screen.getByText('🔑 Forgot your password?')
    expect(forgotPasswordLink).toHaveAttribute('href', '/forgot-password')

    const registerLink = screen.getByText('Create your account 🎉')
    expect(registerLink).toHaveAttribute('href', '/register')
  })

  it('shows loading state when submitting', async () => {
    const { api } = await import('../../api/client')
    vi.mocked(api.post).mockImplementation(() => new Promise(() => {})) // Never resolves

    renderLoginPage()

    const emailInput = screen.getByLabelText('Username or Email')
    const passwordInput = screen.getByLabelText('Password')
    const signInButton = screen.getByRole('button', { name: '🔐 Sign In' })

    fireEvent.change(emailInput, { target: { value: 'test@example.com' } })
    fireEvent.change(passwordInput, { target: { value: 'password123' } })
    fireEvent.click(signInButton)

    await waitFor(() => {
      expect(screen.getByText('🚀 Signing you in...')).toBeInTheDocument()
    })
  })

  it('displays error message on login failure', async () => {
    const { api } = await import('../../api/client')
    vi.mocked(api.post).mockRejectedValue({ message: 'Invalid credentials' })

    renderLoginPage()

    const emailInput = screen.getByLabelText('Username or Email')
    const passwordInput = screen.getByLabelText('Password')
    const signInButton = screen.getByRole('button', { name: '🔐 Sign In' })

    fireEvent.change(emailInput, { target: { value: 'test@example.com' } })
    fireEvent.change(passwordInput, { target: { value: 'wrongpassword' } })
    fireEvent.click(signInButton)

    await waitFor(() => {
      expect(screen.getByText('Invalid credentials')).toBeInTheDocument()
    })
  })
})
