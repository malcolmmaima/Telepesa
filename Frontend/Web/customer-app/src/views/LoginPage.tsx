import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { api } from '../api/client'
import { useAuth } from '../store/auth'
import { Input } from '../components/ui/Input'
import { Button } from '../components/ui/Button'
import type { ApiError } from '../types'

const loginSchema = z.object({
  usernameOrEmail: z.string().min(1, 'Username or email is required'),
  password: z.string().min(1, 'Password is required'),
})

type LoginForm = z.infer<typeof loginSchema>

export function LoginPage() {
  const [isLoading, setIsLoading] = useState(false)
  const [serverError, setServerError] = useState<string | null>(null)
  const navigate = useNavigate()
  const { setSession } = useAuth()

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<LoginForm>({
    resolver: zodResolver(loginSchema),
  })

  const onSubmit = async (data: LoginForm) => {
    setIsLoading(true)
    setServerError(null)
    
    try {
      const response = await api.post('/users/login', data)
      const { accessToken, refreshToken, user } = response.data
      
      setSession({ accessToken, refreshToken, user })
      navigate('/', { replace: true })
    } catch (error: unknown) {
      const apiError = error as ApiError
      setServerError(apiError.message || 'Login failed. Please try again.')
    } finally {
      setIsLoading(false)
    }
  }

  return (
    <div className="min-h-screen grid place-items-center bg-gradient-to-br from-white via-financial-background to-gray-50">
      <div className="w-full max-w-md">
        {/* Logo */}
        <div className="text-center mb-8">
          <div className="w-16 h-16 bg-financial-gradient rounded-financial-lg flex items-center justify-center mx-auto mb-4">
            <span className="text-white font-bold text-2xl">T</span>
          </div>
          <h1 className="text-3xl font-bold text-financial-navy mb-2">Telepesa</h1>
          <p className="text-financial-gray">Secure Banking, Simplified</p>
        </div>

        {/* Login Form */}
        <div className="card p-8">
          <div className="mb-6">
            <h2 className="text-xl font-semibold text-financial-navy mb-1">Welcome back</h2>
            <p className="text-sm text-financial-gray">Sign in to your account to continue</p>
          </div>

          <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
            <Input
              label="Username or Email"
              placeholder="you@example.com"
              error={errors.usernameOrEmail?.message}
              {...register('usernameOrEmail')}
            />
            
            <Input
              type="password"
              label="Password"
              placeholder="••••••••"
              error={errors.password?.message}
              {...register('password')}
            />

            {serverError && (
              <div className="p-3 rounded-financial bg-financial-danger/10 border border-financial-danger/20">
                <p className="text-sm text-financial-danger flex items-center">
                  <svg className="mr-2 h-4 w-4" fill="currentColor" viewBox="0 0 20 20">
                    <path fillRule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7 4a1 1 0 11-2 0 1 1 0 012 0zm-1-9a1 1 0 00-1 1v4a1 1 0 102 0V6a1 1 0 00-1-1z" clipRule="evenodd" />
                  </svg>
                  {serverError}
                </p>
              </div>
            )}

            <Button
              type="submit"
              className="w-full"
              loading={isLoading}
              disabled={isLoading}
            >
              {isLoading ? 'Signing in...' : 'Sign In'}
            </Button>
          </form>

          <div className="mt-6 text-center">
            <a href="#" className="text-sm text-financial-blue hover:text-financial-navy transition-colors">
              Forgot your password?
            </a>
          </div>
        </div>

        {/* Footer */}
        <div className="text-center mt-8 text-sm text-financial-gray">
          <p>© 2024 Telepesa. All rights reserved.</p>
        </div>
      </div>
    </div>
  )
}


