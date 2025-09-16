import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import Lottie from 'lottie-react'
import { api } from '../api/client'
import { useAuth } from '../store/auth'
import { securityApi } from '../api/security'
import { Input } from '../components/ui/Input'
import { Button } from '../components/ui/Button'
import { SecurityQuestionsModal } from '../components/security/SecurityQuestionsModal'
import type { ApiError } from '../types'

const registerSchema = z
  .object({
    firstName: z.string().min(1, 'First name is required'),
    lastName: z.string().min(1, 'Last name is required'),
    email: z.string().email('Please enter a valid email address'),
    username: z.string().min(3, 'Username must be at least 3 characters'),
    password: z.string().min(6, 'Password must be at least 6 characters'),
    confirmPassword: z.string().min(6, 'Please confirm your password'),
    phoneNumber: z.string().min(10, 'Please enter a valid phone number'),
  })
  .refine(data => data.password === data.confirmPassword, {
    message: "Passwords don't match",
    path: ['confirmPassword'],
  })

type RegisterForm = z.infer<typeof registerSchema>

export function RegisterPage() {
  const [isLoading, setIsLoading] = useState(false)
  const [serverError, setServerError] = useState<string | null>(null)
  const [showSecurityQuestions, setShowSecurityQuestions] = useState(false)
  const [registrationData, setRegistrationData] = useState<any>(null)
  const navigate = useNavigate()
  const { setSession } = useAuth()

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<RegisterForm>({
    resolver: zodResolver(registerSchema),
  })

  const onSubmit = async (data: RegisterForm) => {
    setIsLoading(true)
    setServerError(null)

    try {
      const { confirmPassword: _confirmPassword, ...regData } = data
      
      // Store registration data and show security questions setup
      setRegistrationData(regData)
      setShowSecurityQuestions(true)
      setIsLoading(false)
    } catch (error: any) {
      setIsLoading(false)
      const apiError = error as ApiError
      setServerError(apiError.message || 'Registration failed. Please try again.')
    }
  }

  const handleSecurityQuestionsSuccess = async () => {
    if (!registrationData) return

    setIsLoading(true)
    try {
      // Complete registration
      await api.post('/users/register', registrationData)

      // Auto-login after successful registration
      const loginResponse = await api.post('/users/login', {
        usernameOrEmail: registrationData.email,
        password: registrationData.password,
      })

      const { accessToken, refreshToken, user } = loginResponse.data
      setSession({ accessToken, refreshToken, user })
      navigate('/', { replace: true })
    } catch (error: unknown) {
      const apiError = error as ApiError
      setServerError(apiError.message || 'Registration failed. Please try again.')
    } finally {
      setIsLoading(false)
    }
  }

  // Growth-themed Lottie animation
  const growthAnimationData = {
    v: '5.5.7',
    fr: 30,
    ip: 0,
    op: 120,
    w: 400,
    h: 400,
    nm: 'Growth Animation',
    ddd: 0,
    assets: [],
    layers: [
      {
        ddd: 0,
        ind: 1,
        ty: 4,
        nm: 'Plant Growing',
        sr: 1,
        ks: {
          o: { a: 0, k: 100, ix: 11 },
          r: { a: 0, k: 0, ix: 10 },
          p: { a: 0, k: [200, 300, 0], ix: 2 },
          a: { a: 0, k: [0, 0, 0], ix: 1 },
          s: {
            a: 1,
            k: [
              {
                i: { x: [0.833], y: [0.833] },
                o: { x: [0.167], y: [0.167] },
                t: 0,
                s: [0, 0, 100],
              },
              { t: 120, s: [100, 100, 100] },
            ],
            ix: 6,
          },
        },
        ao: 0,
        shapes: [
          {
            ty: 'rc',
            p: { a: 0, k: [0, 0], ix: 3 },
            s: { a: 0, k: [20, 80], ix: 2 },
            r: { a: 0, k: 10, ix: 4 },
          },
          {
            ty: 'fl',
            c: { a: 0, k: [0.067, 0.733, 0.504, 1], ix: 4 },
            o: { a: 0, k: 100, ix: 5 },
            r: 1,
            bm: 0,
            nm: 'Fill 1',
            mn: 'ADBE Vector Graphic - Fill',
            hd: false,
          },
        ],
        ip: 0,
        op: 121,
        st: 0,
        bm: 0,
      },
    ],
    markers: [],
  }

  return (
    <div className="min-h-screen flex">
      {/* Left Side - Animation */}
      <div className="hidden lg:flex lg:w-1/2 bg-gradient-to-br from-financial-success via-financial-blue to-primary-500 relative overflow-hidden">
        <div className="absolute inset-0">
          {/* Decorative elements */}
          <div className="absolute top-16 right-20 w-40 h-40 bg-white/10 rounded-full blur-xl"></div>
          <div className="absolute bottom-20 left-16 w-56 h-56 bg-white/5 rounded-full blur-2xl"></div>
          <div className="absolute top-1/3 right-8 w-28 h-28 bg-financial-warning/20 rounded-full blur-lg"></div>
        </div>

        <div className="relative z-10 flex flex-col justify-center items-center p-16 text-white">
          {/* Logo */}
          <div className="mb-8">
            <div className="w-20 h-20 bg-white/20 backdrop-blur-sm rounded-2xl flex items-center justify-center mb-6">
              <span className="text-white font-bold text-3xl">T</span>
            </div>
            <h1 className="text-4xl font-bold mb-2">Join Telepesa</h1>
            <p className="text-white/90 text-lg">Start Your Financial Journey</p>
          </div>

          {/* Animation Container */}
          <div className="w-80 h-80 mb-8">
            <Lottie animationData={growthAnimationData} className="w-full h-full" loop={true} />
          </div>

          {/* Features */}
          <div className="text-center max-w-md">
            <h2 className="text-2xl font-semibold mb-4">🌱 Grow Your Wealth</h2>
            <p className="text-white/80 text-base leading-relaxed">
              Join thousands of happy customers who trust us with their financial growth. Your
              success story starts today! ✨
            </p>
          </div>
        </div>
      </div>

      {/* Right Side - Form */}
      <div className="w-full lg:w-1/2 flex items-center justify-center p-8 bg-gradient-to-br from-white via-financial-background to-gray-50">
        <div className="w-full max-w-md space-y-6">
          {/* Mobile Logo */}
          <div className="lg:hidden text-center">
            <div className="w-16 h-16 bg-financial-gradient rounded-2xl flex items-center justify-center mx-auto mb-4">
              <span className="text-white font-bold text-2xl">T</span>
            </div>
            <h1 className="text-3xl font-bold text-financial-navy mb-2">Join Telepesa</h1>
            <p className="text-financial-gray">Start Your Financial Journey</p>
          </div>

          {/* Registration Card */}
          <div className="card p-8 space-y-6">
            <div className="text-center lg:text-left">
              <h2 className="text-2xl font-bold text-financial-navy mb-2">
                Create Your Account 🎉
              </h2>
              <p className="text-financial-gray">
                Let's get you set up with your new Telepesa account!
              </p>
            </div>

            <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <Input
                  label="First Name"
                  placeholder="John"
                  error={errors.firstName?.message}
                  {...register('firstName')}
                  className="text-base"
                />

                <Input
                  label="Last Name"
                  placeholder="Doe"
                  error={errors.lastName?.message}
                  {...register('lastName')}
                  className="text-base"
                />
              </div>

              <Input
                label="Email Address"
                type="email"
                placeholder="john@example.com"
                error={errors.email?.message}
                {...register('email')}
                className="text-base"
              />

              <Input
                label="Username"
                placeholder="johndoe123"
                error={errors.username?.message}
                {...register('username')}
                className="text-base"
              />

              <Input
                label="Phone Number"
                type="tel"
                placeholder="+1 (555) 123-4567"
                error={errors.phoneNumber?.message}
                {...register('phoneNumber')}
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

              <Input
                type="password"
                label="Confirm Password"
                placeholder="••••••••"
                error={errors.confirmPassword?.message}
                {...register('confirmPassword')}
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
                {isLoading ? '🚀 Creating your account...' : '🎉 Create Account'}
              </Button>
            </form>

            <div className="space-y-4">
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
                  Already have an account?{' '}
                  <Link
                    to="/login"
                    className="font-medium text-financial-blue hover:text-financial-navy transition-colors"
                  >
                    Sign in
                  </Link>
                </p>
              </div>
            </div>
          </div>

          {/* Footer */}
          <div className="text-center text-xs text-financial-gray">
            <p>🔒 Your data is protected with bank-level security</p>
            <p className="mt-1">© 2024 Telepesa. All rights reserved.</p>
          </div>
        </div>
      </div>

      {/* Security Questions Modal */}
      <SecurityQuestionsModal
        isOpen={showSecurityQuestions}
        onClose={() => {
          setShowSecurityQuestions(false)
          setRegistrationData(null)
        }}
        onSuccess={handleSecurityQuestionsSuccess}
        mode="setup"
        title="🛡️ Setup Security Questions"
        description="Choose 3 security questions to help protect your account. These will be used for account recovery."
      />
    </div>
  )
}
