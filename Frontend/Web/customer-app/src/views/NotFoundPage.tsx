import { Button } from '../components/ui/Button'
import { Card } from '../components/ui/Card'

export function NotFoundPage() {
  return (
    <div className="min-h-screen bg-gradient-to-br from-financial-background via-white to-financial-background flex items-center justify-center p-4">
      <div className="max-w-4xl w-full">
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-12 items-center">
          {/* Left Side - Illustration */}
          <div className="text-center lg:text-left">
            <div className="relative">
              {/* Floating Numbers Animation */}
              <div className="absolute -top-20 left-1/4 text-6xl font-bold text-financial-blue/20 animate-float">
                4
              </div>
              <div className="absolute -top-16 right-1/4 text-8xl font-bold text-financial-navy/30 animate-float-delayed">
                0
              </div>
              <div className="absolute -top-12 left-1/2 transform -translate-x-1/2 text-6xl font-bold text-financial-blue/20 animate-float-slow">
                4
              </div>
              
              {/* Main Illustration */}
              <div className="relative z-10">
                <div className="w-80 h-80 mx-auto lg:mx-0 relative">
                  {/* Piggy Bank Illustration */}
                  <div className="w-full h-full bg-gradient-to-br from-pink-300 to-pink-400 rounded-full relative transform hover:scale-105 transition-transform duration-300">
                    {/* Piggy Bank Body */}
                    <div className="absolute inset-0 bg-gradient-to-br from-pink-200 to-pink-300 rounded-full">
                      {/* Eyes */}
                      <div className="absolute top-20 left-20 w-8 h-8 bg-black rounded-full animate-blink"></div>
                      <div className="absolute top-20 right-20 w-8 h-8 bg-black rounded-full animate-blink"></div>
                      
                      {/* Snout */}
                      <div className="absolute bottom-32 left-1/2 transform -translate-x-1/2">
                        <div className="w-16 h-10 bg-pink-400 rounded-full relative">
                          <div className="absolute top-3 left-4 w-2 h-2 bg-black rounded-full"></div>
                          <div className="absolute top-3 right-4 w-2 h-2 bg-black rounded-full"></div>
                        </div>
                      </div>
                      
                      {/* Coin Slot */}
                      <div className="absolute top-8 left-1/2 transform -translate-x-1/2 w-16 h-3 bg-pink-500 rounded-full"></div>
                      
                      {/* Legs */}
                      <div className="absolute -bottom-4 left-12 w-8 h-12 bg-pink-300 rounded-lg"></div>
                      <div className="absolute -bottom-4 right-12 w-8 h-12 bg-pink-300 rounded-lg"></div>
                      <div className="absolute -bottom-4 left-24 w-8 h-12 bg-pink-300 rounded-lg"></div>
                      <div className="absolute -bottom-4 right-24 w-8 h-12 bg-pink-300 rounded-lg"></div>
                      
                      {/* Tail */}
                      <div className="absolute top-32 -right-4 w-3 h-16 bg-pink-300 rounded-full transform rotate-45 origin-bottom"></div>
                    </div>
                    
                    {/* Floating Coins */}
                    <div className="absolute -top-8 left-4 w-6 h-6 bg-yellow-400 rounded-full animate-bounce"></div>
                    <div className="absolute -top-12 right-8 w-4 h-4 bg-yellow-300 rounded-full animate-bounce-delayed"></div>
                    <div className="absolute -right-8 top-16 w-5 h-5 bg-yellow-400 rounded-full animate-bounce-slow"></div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          {/* Right Side - Content */}
          <div className="text-center lg:text-left space-y-8">
            <div>
              <h1 className="text-6xl lg:text-8xl font-bold text-financial-navy mb-4">
                4<span className="text-financial-blue animate-pulse">0</span>4
              </h1>
              <h2 className="text-2xl lg:text-3xl font-bold text-financial-navy mb-4">
                Oops! Page Not Found 🤷‍♀️
              </h2>
              <p className="text-lg text-financial-gray leading-relaxed">
                Looks like this page took a vacation to the Bahamas! 🏖️
                <br />
                Don't worry, your money is still safe with us. 
                <br />
                Let's get you back to managing your finances!
              </p>
            </div>

            {/* Fun Stats */}
            <Card className="p-6 bg-gradient-to-r from-financial-navy/5 to-financial-blue/5 border-l-4 border-l-financial-blue">
              <div className="grid grid-cols-2 gap-4 text-center">
                <div>
                  <div className="text-2xl font-bold text-financial-navy">💰</div>
                  <div className="text-sm text-financial-gray">Your money is</div>
                  <div className="text-lg font-semibold text-green-600">Safe & Secure</div>
                </div>
                <div>
                  <div className="text-2xl font-bold text-financial-navy">🏦</div>
                  <div className="text-sm text-financial-gray">Banking since</div>
                  <div className="text-lg font-semibold text-financial-navy">2024</div>
                </div>
              </div>
            </Card>

            {/* Action Buttons */}
            <div className="flex flex-col sm:flex-row gap-4 justify-center lg:justify-start">
              <Button
                onClick={() => window.location.href = '/'}
                className="bg-gradient-to-r from-financial-navy to-financial-blue hover:from-financial-blue hover:to-financial-navy transition-all duration-300 transform hover:scale-105"
              >
                🏠 Back to Dashboard
              </Button>
              
              <Button
                variant="outline"
                onClick={() => window.location.href = '/accounts'}
                className="hover:bg-financial-blue hover:text-white transition-all duration-300"
              >
                🏦 View My Accounts
              </Button>
              
              <Button
                variant="ghost"
                onClick={() => window.location.href = '/transactions'}
                className="hover:bg-financial-gray/10"
              >
                📊 Transaction History
              </Button>
            </div>

            {/* Quick Help */}
            <div className="text-center lg:text-left">
              <p className="text-sm text-financial-gray mb-2">
                Need help finding something specific? 🤔
              </p>
              <div className="flex flex-wrap gap-2 justify-center lg:justify-start">
                <span 
                  onClick={() => window.location.href = '/transfers'}
                  className="inline-block px-3 py-1 bg-financial-blue/10 text-financial-blue rounded-full text-xs hover:bg-financial-blue/20 cursor-pointer transition-colors"
                >
                  💸 Transfers
                </span>
                <span 
                  onClick={() => window.location.href = '/payments'}
                  className="inline-block px-3 py-1 bg-financial-blue/10 text-financial-blue rounded-full text-xs hover:bg-financial-blue/20 cursor-pointer transition-colors"
                >
                  💳 Bill Payments
                </span>
                <span 
                  onClick={() => window.location.href = '/loans'}
                  className="inline-block px-3 py-1 bg-financial-blue/10 text-financial-blue rounded-full text-xs hover:bg-financial-blue/20 cursor-pointer transition-colors"
                >
                  🏠 Loans
                </span>
                <span 
                  onClick={() => window.location.href = '/profile'}
                  className="inline-block px-3 py-1 bg-financial-blue/10 text-financial-blue rounded-full text-xs hover:bg-financial-blue/20 cursor-pointer transition-colors"
                >
                  👤 Profile
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      {/* Background Decorations */}
      <div className="fixed inset-0 overflow-hidden pointer-events-none">
        <div className="absolute top-1/4 left-10 w-2 h-2 bg-financial-blue rounded-full animate-ping"></div>
        <div className="absolute top-1/3 right-16 w-3 h-3 bg-financial-navy rounded-full animate-pulse"></div>
        <div className="absolute bottom-1/4 left-1/4 w-1 h-1 bg-financial-blue rounded-full animate-ping"></div>
        <div className="absolute bottom-1/3 right-1/3 w-2 h-2 bg-financial-navy rounded-full animate-pulse"></div>
      </div>
    </div>
  )
}

// Add these custom animations to your CSS file
const customAnimations = `
  @keyframes float {
    0%, 100% { transform: translateY(0px) rotate(0deg); }
    25% { transform: translateY(-10px) rotate(2deg); }
    50% { transform: translateY(-20px) rotate(-2deg); }
    75% { transform: translateY(-10px) rotate(1deg); }
  }
  
  @keyframes float-delayed {
    0%, 100% { transform: translateY(0px) rotate(0deg); }
    25% { transform: translateY(-15px) rotate(-2deg); }
    50% { transform: translateY(-30px) rotate(2deg); }
    75% { transform: translateY(-15px) rotate(-1deg); }
  }
  
  @keyframes float-slow {
    0%, 100% { transform: translateY(0px) rotate(0deg); }
    25% { transform: translateY(-8px) rotate(1deg); }
    50% { transform: translateY(-16px) rotate(-1deg); }
    75% { transform: translateY(-8px) rotate(2deg); }
  }
  
  @keyframes bounce-delayed {
    0%, 100% { transform: translateY(0px); }
    50% { transform: translateY(-20px); }
  }
  
  @keyframes bounce-slow {
    0%, 100% { transform: translateY(0px); }
    50% { transform: translateY(-15px); }
  }
  
  @keyframes blink {
    0%, 90%, 100% { transform: scaleY(1); }
    95% { transform: scaleY(0.1); }
  }
  
  .animate-float {
    animation: float 6s ease-in-out infinite;
  }
  
  .animate-float-delayed {
    animation: float-delayed 8s ease-in-out infinite;
    animation-delay: -2s;
  }
  
  .animate-float-slow {
    animation: float-slow 10s ease-in-out infinite;
    animation-delay: -5s;
  }
  
  .animate-bounce-delayed {
    animation: bounce-delayed 2s infinite;
    animation-delay: 0.5s;
  }
  
  .animate-bounce-slow {
    animation: bounce-slow 3s infinite;
    animation-delay: 1s;
  }
  
  .animate-blink {
    animation: blink 4s infinite;
  }
`

// Export the CSS for injection
export const notFoundPageStyles = customAnimations
