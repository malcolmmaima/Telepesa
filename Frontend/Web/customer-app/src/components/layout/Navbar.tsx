import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useAuth, useAuthSelectors } from '../../store/auth'
import { useDarkMode } from '../../store/darkMode'
import { useNotifications } from '../../hooks/useNotifications'
import { Avatar } from '../ui/Avatar'
import { NotificationsDropdown } from '../ui/NotificationsDropdown'
import { cn } from '../../lib/utils'
import { getUserInitials } from '../../lib/avatarUtils'

export const Navbar = () => {
  const [isProfileOpen, setIsProfileOpen] = useState(false)
  const [isNotificationsOpen, setIsNotificationsOpen] = useState(false)
  const { logout, user } = useAuth()
  const { userName } = useAuthSelectors()
  const { darkMode, toggleDarkMode } = useDarkMode()
  const { unreadCount } = useNotifications()

  const userInitials = getUserInitials(user?.firstName, user?.lastName, user?.username)

  const handleLogout = () => {
    logout()
  }

  return (
    <nav className="bg-white dark:bg-slate-800 border-b border-gray-200 dark:border-slate-700 sticky top-0 z-50 transition-colors duration-200">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between items-center h-16">
          {/* Logo */}
          <div className="flex items-center">
            <Link to="/" className="flex items-center space-x-2">
              <div className="w-8 h-8 bg-financial-gradient rounded-lg flex items-center justify-center">
                <span className="text-white font-bold text-lg">T</span>
              </div>
              <span className="text-xl font-bold text-financial-navy dark:text-white">
                Telepesa
              </span>
            </Link>
          </div>

          {/* Navigation Links */}
          <div className="hidden md:flex items-center space-x-8">
            <Link
              to="/"
              className="text-financial-gray dark:text-slate-300 hover:text-financial-navy dark:hover:text-white transition-colors duration-200"
            >
              Dashboard
            </Link>
            <Link
              to="/accounts"
              className="text-financial-gray dark:text-slate-300 hover:text-financial-navy dark:hover:text-white transition-colors duration-200"
            >
              Accounts
            </Link>
            <Link
              to="/transfers"
              className="text-financial-gray dark:text-slate-300 hover:text-financial-navy dark:hover:text-white transition-colors duration-200"
            >
              Transfer
            </Link>
            <Link
              to="/payments"
              className="text-financial-gray dark:text-slate-300 hover:text-financial-navy dark:hover:text-white transition-colors duration-200"
            >
              Pay Bills
            </Link>
            <Link
              to="/loans"
              className="text-financial-gray dark:text-slate-300 hover:text-financial-navy dark:hover:text-white transition-colors duration-200"
            >
              Loans
            </Link>
          </div>

          {/* Right Controls: Theme, Notifications, Profile */}
          <div className="flex items-center gap-2">
            {/* Dark mode toggle */}
            <button
              onClick={toggleDarkMode}
              className="p-2 rounded-financial hover:bg-gray-50 dark:hover:bg-slate-700 transition-colors duration-200"
              aria-label="Toggle dark mode"
              title={darkMode ? 'Switch to light mode' : 'Switch to dark mode'}
            >
              {/* Sun / Moon icon */}
              {darkMode ? (
                <svg className="w-5 h-5 text-yellow-300" viewBox="0 0 24 24" fill="currentColor">
                  <path d="M21.64 13a1 1 0 0 0-1.05-.14 8 8 0 1 1-9.45-9.45A1 1 0 0 0 10 2a10 10 0 1 0 12 12 1 1 0 0 0-.36-1.99z" />
                </svg>
              ) : (
                <svg
                  className="w-5 h-5 text-financial-navy"
                  viewBox="0 0 24 24"
                  fill="currentColor"
                >
                  <path d="M6.76 4.84l-1.8-1.79-1.42 1.41 1.79 1.8 1.43-1.42zm10.45 12.73l1.79 1.8 1.42-1.42-1.8-1.79-1.41 1.41zM12 4V1h-1v3h1zm0 19v-3h-1v3h1zm8-8h3v-1h-3v1zM1 12H4v-1H1v1zm15.24-7.16l1.8-1.79-1.42-1.41-1.79 1.8 1.41 1.4zM4.22 18.36l-1.79 1.8 1.41 1.42 1.8-1.79-1.42-1.43zM12 6a6 6 0 100 12 6 6 0 000-12z" />
                </svg>
              )}
            </button>

            {/* Notifications button */}
            <div className="relative">
              <button
                onClick={() => setIsNotificationsOpen(v => !v)}
                className="p-2 rounded-financial hover:bg-gray-50 dark:hover:bg-slate-700 transition-colors duration-200"
                aria-label="Notifications"
                title="Notifications"
              >
                <svg
                  className="w-5 h-5 text-financial-navy dark:text-slate-200"
                  viewBox="0 0 24 24"
                  fill="currentColor"
                >
                  <path d="M12 2a7 7 0 00-7 7v3.586l-1.707 1.707A1 1 0 004 16h16a1 1 0 00.707-1.707L19 12.586V9a7 7 0 00-7-7zm0 20a3 3 0 01-3-3h6a3 3 0 01-3 3z" />
                </svg>
                {unreadCount > 0 && (
                  <span className="absolute -top-0.5 -right-0.5 inline-flex items-center justify-center h-5 w-5 text-xs font-bold text-white bg-red-500 rounded-full">
                    {unreadCount > 9 ? '9+' : unreadCount}
                  </span>
                )}
              </button>

              <NotificationsDropdown
                isOpen={isNotificationsOpen}
                onClose={() => setIsNotificationsOpen(false)}
              />
            </div>

            {/* Profile Dropdown */}
            <div className="relative">
              <button
                onClick={() => setIsProfileOpen(!isProfileOpen)}
                className="flex items-center space-x-2 p-2 rounded-financial hover:bg-gray-50 dark:hover:bg-slate-700 transition-colors duration-200"
              >
                <Avatar src={user?.avatarUrl} initials={userInitials || undefined} size="sm" />
                <span className="hidden sm:block text-sm font-medium text-financial-navy dark:text-slate-200">
                  {userName}
                </span>
                <svg
                  className={cn(
                    'w-4 h-4 text-financial-gray dark:text-slate-300 transition-transform duration-200',
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
                <div className="absolute right-0 mt-2 w-48 bg-white dark:bg-slate-800 rounded-financial-lg shadow-financial-lg border border-gray-100 dark:border-slate-700 py-2">
                  <Link
                    to="/profile"
                    className="block px-4 py-2 text-sm text-financial-navy dark:text-slate-200 hover:bg-gray-50 dark:hover:bg-slate-700 transition-colors duration-200"
                    onClick={() => setIsProfileOpen(false)}
                  >
                    Profile Settings
                  </Link>
                  <Link
                    to="/security"
                    className="block px-4 py-2 text-sm text-financial-navy dark:text-slate-200 hover:bg-gray-50 dark:hover:bg-slate-700 transition-colors duration-200"
                    onClick={() => setIsProfileOpen(false)}
                  >
                    Security
                  </Link>
                  <Link
                    to="/support"
                    className="block px-4 py-2 text-sm text-financial-navy dark:text-slate-200 hover:bg-gray-50 dark:hover:bg-slate-700 transition-colors duration-200"
                    onClick={() => setIsProfileOpen(false)}
                  >
                    Help & Support
                  </Link>
                  <hr className="my-2 border-gray-100 dark:border-slate-600" />
                  <button
                    onClick={handleLogout}
                    className="block w-full text-left px-4 py-2 text-sm text-financial-danger dark:text-red-400 hover:bg-gray-50 dark:hover:bg-slate-700 transition-colors duration-200"
                  >
                    Sign Out
                  </button>
                </div>
              )}
            </div>
          </div>
        </div>
      </div>
    </nav>
  )
}
