import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useAuth, useAuthSelectors } from '../../store/auth'
import { Avatar } from '../ui/Avatar'
import { cn } from '../../lib/utils'

export const Navbar = () => {
  const [isProfileOpen, setIsProfileOpen] = useState(false)
  const { logout } = useAuth()
  const { userName, userInitials } = useAuthSelectors()

  const handleLogout = () => {
    logout()
  }

  return (
    <nav className="bg-white border-b border-gray-200 sticky top-0 z-50">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between items-center h-16">
          {/* Logo */}
          <div className="flex items-center">
            <Link to="/" className="flex items-center space-x-2">
              <div className="w-8 h-8 bg-financial-gradient rounded-lg flex items-center justify-center">
                <span className="text-white font-bold text-lg">T</span>
              </div>
              <span className="text-xl font-bold text-financial-navy">Telepesa</span>
            </Link>
          </div>

          {/* Navigation Links */}
          <div className="hidden md:flex items-center space-x-8">
            <Link
              to="/"
              className="text-financial-gray hover:text-financial-navy transition-colors duration-200"
            >
              Dashboard
            </Link>
            <Link
              to="/accounts"
              className="text-financial-gray hover:text-financial-navy transition-colors duration-200"
            >
              Accounts
            </Link>
            <Link
              to="/transfers"
              className="text-financial-gray hover:text-financial-navy transition-colors duration-200"
            >
              Transfer
            </Link>
            <Link
              to="/payments"
              className="text-financial-gray hover:text-financial-navy transition-colors duration-200"
            >
              Pay Bills
            </Link>
            <Link
              to="/loans"
              className="text-financial-gray hover:text-financial-navy transition-colors duration-200"
            >
              Loans
            </Link>
          </div>

          {/* Profile Dropdown */}
          <div className="relative">
            <button
              onClick={() => setIsProfileOpen(!isProfileOpen)}
              className="flex items-center space-x-2 p-2 rounded-financial hover:bg-gray-50 transition-colors duration-200"
            >
              <Avatar initials={userInitials || undefined} size="sm" />
              <span className="hidden sm:block text-sm font-medium text-financial-navy">
                {userName}
              </span>
              <svg
                className={cn(
                  'w-4 h-4 text-financial-gray transition-transform duration-200',
                  isProfileOpen && 'rotate-180'
                )}
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="m19 9-7 7-7-7"
                />
              </svg>
            </button>

            {/* Dropdown Menu */}
            {isProfileOpen && (
              <div className="absolute right-0 mt-2 w-48 bg-white rounded-financial-lg shadow-financial-lg border border-gray-100 py-2">
                <Link
                  to="/profile"
                  className="block px-4 py-2 text-sm text-financial-navy hover:bg-gray-50 transition-colors duration-200"
                  onClick={() => setIsProfileOpen(false)}
                >
                  Profile Settings
                </Link>
                <Link
                  to="/security"
                  className="block px-4 py-2 text-sm text-financial-navy hover:bg-gray-50 transition-colors duration-200"
                  onClick={() => setIsProfileOpen(false)}
                >
                  Security
                </Link>
                <Link
                  to="/support"
                  className="block px-4 py-2 text-sm text-financial-navy hover:bg-gray-50 transition-colors duration-200"
                  onClick={() => setIsProfileOpen(false)}
                >
                  Help & Support
                </Link>
                <hr className="my-2 border-gray-100" />
                <button
                  onClick={handleLogout}
                  className="block w-full text-left px-4 py-2 text-sm text-financial-danger hover:bg-gray-50 transition-colors duration-200"
                >
                  Sign Out
                </button>
              </div>
            )}
          </div>
        </div>
      </div>
    </nav>
  )
}
