import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import Lottie from 'lottie-react'
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

  // Simple financial Lottie animation data (inline for now)
  const financialAnimationData = {
    v: '5.5.7',
    fr: 30,
    ip: 0,
    op: 90,
    w: 400,
    h: 400,
    nm: 'Financial Animation',
    ddd: 0,
    assets: [],
    layers: [
      {
        ddd: 0,
        ind: 1,
        ty: 4,
        nm: 'Circle',
        sr: 1,
        ks: {
          o: { a: 0, k: 100, ix: 11 },
          r: {
            a: 1,
            k: [
              { i: { x: [0.833], y: [0.833] }, o: { x: [0.167], y: [0.167] }, t: 0, s: [0] },
              { t: 90, s: [360] },
            ],
            ix: 10,
          },
          p: { a: 0, k: [200, 200, 0], ix: 2 },
          a: { a: 0, k: [0, 0, 0], ix: 1 },
          s: { a: 0, k: [100, 100, 100], ix: 6 },
        },
        ao: 0,
        shapes: [
          {
            ty: 'el',
            p: { a: 0, k: [0, 0], ix: 3 },
            s: { a: 0, k: [100, 100], ix: 2 },
          },
          {
            ty: 'fl',
            c: { a: 0, k: [0.043, 0.231, 0.369, 1], ix: 4 },
            o: { a: 0, k: 100, ix: 5 },
            r: 1,
            bm: 0,
            nm: 'Fill 1',
            mn: 'ADBE Vector Graphic - Fill',
            hd: false,
          },
        ],
        ip: 0,
        op: 91,
        st: 0,
        bm: 0,
      },
    ],
    markers: [],
  }

  return (
    <div className="min-h-screen flex">
      {/* Left Side - Animation */}
      <div className="hidden lg:flex lg:w-1/2 bg-gradient-to-br from-financial-navy via-financial-blue to-primary-600 relative overflow-hidden">
        <div className="absolute inset-0">
          {/* Decorative elements */}
          <div className="absolute top-20 left-20 w-32 h-32 bg-white/10 rounded-full blur-xl"></div>
          <div className="absolute bottom-32 right-16 w-48 h-48 bg-white/5 rounded-full blur-2xl"></div>
          <div className="absolute top-1/2 left-8 w-24 h-24 bg-financial-warning/20 rounded-full blur-lg"></div>
        </div>

        <div className="relative z-10 flex flex-col justify-center items-center p-16 text-white">
          {/* Logo */}
          <div className="mb-8">
            <div className="w-20 h-20 bg-white/20 backdrop-blur-sm rounded-2xl flex items-center justify-center mb-6">
              <span className="text-white font-bold text-3xl">T</span>
            </div>
            <h1 className="text-4xl font-bold mb-2">Telepesa</h1>
            <p className="text-white/90 text-lg">Your Money, Your Way</p>
          </div>

          {/* Animation Container */}
          <div className="w-80 h-80 mb-8">
            <Lottie animationData={financialAnimationData} className="w-full h-full" loop={true} />
          </div>

          {/* Features */}
          <div className="text-center max-w-md">
            <h2 className="text-2xl font-semibold mb-4">💰 Smart Banking</h2>
            <p className="text-white/80 text-base leading-relaxed">
              Experience seamless banking with our fun, secure, and user-friendly platform. Your
              financial journey starts here! 🚀
            </p>
          </div>
        </div>
      </div>

      {/* Right Side - Form */}
      <div className="w-full lg:w-1/2 flex items-center justify-center p-8 bg-gradient-to-br from-white via-financial-background to-gray-50">
        <div className="w-full max-w-md space-y-8">
          {/* Mobile Logo */}
          <div className="lg:hidden text-center">
            <div className="w-16 h-16 bg-financial-gradient rounded-2xl flex items-center justify-center mx-auto mb-4">
              <span className="text-white font-bold text-2xl">T</span>
            </div>
            <h1 className="text-3xl font-bold text-financial-navy mb-2">Telepesa</h1>
            <p className="text-financial-gray">Your Money, Your Way</p>
          </div>

          {/* Welcome Card */}
          <div className="card p-8 space-y-6">
            <div className="text-center lg:text-left">
              <h2 className="text-2xl font-bold text-financial-navy mb-2">Welcome back! 👋</h2>
              <p className="text-financial-gray">
                Ready to manage your finances? Let's get you signed in!
              </p>
            </div>

            <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
              <Input
                label="Username or Email"
                placeholder="you@example.com"
                error={errors.usernameOrEmail?.message}
                {...register('usernameOrEmail')}
                className="text-base"
              />

              <Input
                type="password"
                label="Password"
                placeholder="••••••••"
                error={errors.password?.message}
                {...register('password')}
                className="text-base"
              />

              {serverError && (
                <div className="p-4 rounded-financial bg-red-50 border border-red-200 animate-pulse">
                  <p className="text-sm text-red-600 flex items-center">
                    <span className="mr-2">⚠️</span>
                    {serverError}
                  </p>
                </div>
              )}

              <Button
                type="submit"
                className="w-full text-lg py-4"
                loading={isLoading}
                disabled={isLoading}
              >
                {isLoading ? '🚀 Signing you in...' : '🔐 Sign In'}
              </Button>
            </form>

            <div className="space-y-4">
              <div className="text-center">
                <Link
                  to="/forgot-password"
                  className="text-financial-blue hover:text-financial-navy transition-colors text-sm font-medium"
                >
                  🔑 Forgot your password?
                </Link>
              </div>

              <div className="relative">
                <div className="absolute inset-0 flex items-center">
                  <div className="w-full border-t border-gray-300"></div>
                </div>
                <div className="relative flex justify-center text-sm">
                  <span className="bg-white px-4 text-financial-gray">or</span>
                </div>
              </div>

              <div className="text-center">
                <p className="text-financial-gray text-sm">
                  New to Telepesa?
                  <Link
                    to="/register"
                    className="ml-1 text-financial-blue hover:text-financial-navy transition-colors font-medium"
                  >
                    Create your account 🎉
                  </Link>
                </p>
              </div>
            </div>
          </div>

          {/* Footer */}
          <div className="text-center text-xs text-financial-gray">
            <p>🔒 Secured by industry-standard encryption</p>
            <p className="mt-1">© 2024 Telepesa. All rights reserved.</p>
          </div>
        </div>
      </div>
    </div>
  )
}
