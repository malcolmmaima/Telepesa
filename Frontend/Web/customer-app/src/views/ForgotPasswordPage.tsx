import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import Lottie from 'lottie-react'
import { api } from '../api/client'
import { Input } from '../components/ui/Input'
import { Button } from '../components/ui/Button'
import type { ApiError } from '../types'

const forgotPasswordSchema = z.object({
  email: z.string().email('Please enter a valid email address'),
})

type ForgotPasswordForm = z.infer<typeof forgotPasswordSchema>

export function ForgotPasswordPage() {
  const [isLoading, setIsLoading] = useState(false)
  const [serverError, setServerError] = useState<string | null>(null)
  const [isSuccess, setIsSuccess] = useState(false)

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<ForgotPasswordForm>({
    resolver: zodResolver(forgotPasswordSchema),
  })

  const onSubmit = async (data: ForgotPasswordForm) => {
    setIsLoading(true)
    setServerError(null)
    
    try {
      await api.post('/users/forgot-password', data)
      setIsSuccess(true)
    } catch (error: unknown) {
      const apiError = error as ApiError
      setServerError(apiError.message || 'Failed to send reset email. Please try again.')
    } finally {
      setIsLoading(false)
    }
  }

  // Recovery-themed Lottie animation
  const recoveryAnimationData = {
    v: '5.5.7',
    fr: 30,
    ip: 0,
    op: 60,
    w: 400,
    h: 400,
    nm: 'Recovery Animation',
    ddd: 0,
    assets: [],
    layers: [
      {
        ddd: 0,
        ind: 1,
        ty: 4,
        nm: 'Key Floating',
        sr: 1,
        ks: {
          o: { a: 0, k: 100, ix: 11 },
          r: { a: 1, k: [{ i: { x: [0.833], y: [0.833] }, o: { x: [0.167], y: [0.167] }, t: 0, s: [0] }, { t: 60, s: [360] }], ix: 10 },
          p: { a: 1, k: [{ i: { x: [0.833], y: [0.833] }, o: { x: [0.167], y: [0.167] }, t: 0, s: [200, 180, 0] }, { t: 60, s: [200, 220, 0] }], ix: 2 },
          a: { a: 0, k: [0, 0, 0], ix: 1 },
          s: { a: 0, k: [100, 100, 100], ix: 6 }
        },
        ao: 0,
        shapes: [
          {
            ty: 'el',
            p: { a: 0, k: [0, 0], ix: 3 },
            s: { a: 0, k: [60, 60], ix: 2 }
          },
          {
            ty: 'fl',
            c: { a: 0, k: [0.918, 0.62, 0.043, 1], ix: 4 },
            o: { a: 0, k: 100, ix: 5 },
            r: 1,
            bm: 0,
            nm: 'Fill 1',
            mn: 'ADBE Vector Graphic - Fill',
            hd: false
          }
        ],
        ip: 0,
        op: 61,
        st: 0,
        bm: 0
      }
    ],
    markers: []
  }

  if (isSuccess) {
    return (
      <div className="min-h-screen flex">
        {/* Left Side - Animation */}
        <div className="hidden lg:flex lg:w-1/2 bg-gradient-to-br from-financial-warning via-financial-blue to-primary-500 relative overflow-hidden">
          <div className="absolute inset-0">
            {/* Decorative elements */}
            <div className="absolute top-20 left-20 w-32 h-32 bg-white/10 rounded-full blur-xl"></div>
            <div className="absolute bottom-32 right-16 w-48 h-48 bg-white/5 rounded-full blur-2xl"></div>
            <div className="absolute top-1/2 left-8 w-24 h-24 bg-financial-success/20 rounded-full blur-lg"></div>
          </div>
          
          <div className="relative z-10 flex flex-col justify-center items-center p-16 text-white">
            {/* Logo */}
            <div className="mb-8">
              <div className="w-20 h-20 bg-white/20 backdrop-blur-sm rounded-2xl flex items-center justify-center mb-6">
                <span className="text-white font-bold text-3xl">T</span>
              </div>
              <h1 className="text-4xl font-bold mb-2">Check Your Email</h1>
              <p className="text-white/90 text-lg">Recovery Link Sent</p>
            </div>

            {/* Success Animation */}
            <div className="w-80 h-80 mb-8">
              <div className="w-full h-full flex items-center justify-center">
                <div className="text-8xl">📧</div>
              </div>
            </div>

            {/* Success Message */}
            <div className="text-center max-w-md">
              <h2 className="text-2xl font-semibold mb-4">✅ Email Sent!</h2>
              <p className="text-white/80 text-base leading-relaxed">
                We've sent a password recovery link to your email. 
                Check your inbox and follow the instructions! 📬
              </p>
            </div>
          </div>
        </div>

        {/* Right Side - Success Message */}
        <div className="w-full lg:w-1/2 flex items-center justify-center p-8 bg-gradient-to-br from-white via-financial-background to-gray-50">
          <div className="w-full max-w-md space-y-8">
            {/* Mobile Logo */}
            <div className="lg:hidden text-center">
              <div className="w-16 h-16 bg-financial-gradient rounded-2xl flex items-center justify-center mx-auto mb-4">
                <span className="text-white font-bold text-2xl">T</span>
              </div>
              <h1 className="text-3xl font-bold text-financial-navy mb-2">Check Your Email</h1>
              <p className="text-financial-gray">Recovery Link Sent</p>
            </div>

            {/* Success Card */}
            <div className="card p-8 space-y-6 text-center">
              <div className="text-6xl mb-4">📧</div>
              <div>
                <h2 className="text-2xl font-bold text-financial-navy mb-2">Email Sent Successfully! ✅</h2>
                <p className="text-financial-gray mb-6">
                  We've sent a password recovery link to your email address. 
                  Please check your inbox and follow the instructions to reset your password.
                </p>
              </div>

              <div className="bg-financial-success/10 border border-financial-success/20 rounded-financial p-4">
                <p className="text-financial-success text-sm">
                  <strong>💡 Pro tip:</strong> Don't forget to check your spam folder if you don't see the email in your inbox!
                </p>
              </div>

              <div className="space-y-4">
                <Link to="/login">
                  <Button className="w-full text-lg py-4">
                    🔙 Back to Sign In
                  </Button>
                </Link>
                
                <p className="text-financial-gray text-sm">
                  Didn't receive the email?
                  <button 
                    onClick={() => setIsSuccess(false)}
                    className="ml-1 text-financial-blue hover:text-financial-navy transition-colors font-medium"
                  >
                    Try again 🔄
                  </button>
                </p>
              </div>
            </div>

            {/* Footer */}
            <div className="text-center text-xs text-financial-gray">
              <p>🔒 Security is our priority</p>
              <p className="mt-1">© 2024 Telepesa. All rights reserved.</p>
            </div>
          </div>
        </div>
      </div>
    )
  }

  return (
    <div className="min-h-screen flex">
      {/* Left Side - Animation */}
      <div className="hidden lg:flex lg:w-1/2 bg-gradient-to-br from-financial-warning via-financial-blue to-primary-500 relative overflow-hidden">
        <div className="absolute inset-0">
          {/* Decorative elements */}
          <div className="absolute top-20 left-20 w-32 h-32 bg-white/10 rounded-full blur-xl"></div>
          <div className="absolute bottom-32 right-16 w-48 h-48 bg-white/5 rounded-full blur-2xl"></div>
          <div className="absolute top-1/2 left-8 w-24 h-24 bg-financial-success/20 rounded-full blur-lg"></div>
        </div>
        
        <div className="relative z-10 flex flex-col justify-center items-center p-16 text-white">
          {/* Logo */}
          <div className="mb-8">
            <div className="w-20 h-20 bg-white/20 backdrop-blur-sm rounded-2xl flex items-center justify-center mb-6">
              <span className="text-white font-bold text-3xl">T</span>
            </div>
            <h1 className="text-4xl font-bold mb-2">Account Recovery</h1>
            <p className="text-white/90 text-lg">We've Got You Covered</p>
          </div>

          {/* Animation Container */}
          <div className="w-80 h-80 mb-8">
            <Lottie
              animationData={recoveryAnimationData}
              className="w-full h-full"
              loop={true}
            />
          </div>

          {/* Features */}
          <div className="text-center max-w-md">
            <h2 className="text-2xl font-semibold mb-4">🔑 Quick Recovery</h2>
            <p className="text-white/80 text-base leading-relaxed">
              Forgot your password? No worries! We'll help you get back into your account 
              safely and securely. 🛡️
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
            <h1 className="text-3xl font-bold text-financial-navy mb-2">Account Recovery</h1>
            <p className="text-financial-gray">We've Got You Covered</p>
          </div>

          {/* Recovery Card */}
          <div className="card p-8 space-y-6">
            <div className="text-center lg:text-left">
              <h2 className="text-2xl font-bold text-financial-navy mb-2">Reset Your Password 🔑</h2>
              <p className="text-financial-gray">
                Enter your email address and we'll send you a link to reset your password.
              </p>
            </div>

            <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
              <Input
                label="Email Address"
                type="email"
                placeholder="you@example.com"
                error={errors.email?.message}
                {...register('email')}
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
                {isLoading ? '📧 Sending recovery email...' : '🔄 Send Recovery Email'}
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
              
              <div className="text-center space-y-2">
                <p className="text-financial-gray text-sm">
                  Remember your password?
                  <Link 
                    to="/login" 
                    className="ml-1 text-financial-blue hover:text-financial-navy transition-colors font-medium"
                  >
                    Sign in here 👈
                  </Link>
                </p>
                
                <p className="text-financial-gray text-sm">
                  Don't have an account?
                  <Link 
                    to="/register" 
                    className="ml-1 text-financial-blue hover:text-financial-navy transition-colors font-medium"
                  >
                    Create one now 🎉
                  </Link>
                </p>
              </div>
            </div>
          </div>

          {/* Footer */}
          <div className="text-center text-xs text-financial-gray">
            <p>🔒 Your security is our top priority</p>
            <p className="mt-1">© 2024 Telepesa. All rights reserved.</p>
          </div>
        </div>
      </div>
    </div>
  )
}
