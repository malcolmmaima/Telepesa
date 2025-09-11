// Utility functions for handling avatar URLs

/**
 * Get the API base URL without the /api/v1 suffix for static file access
 */
const getStaticBaseUrl = (): string => {
  const apiBaseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1'
  // Remove /api/v1 suffix to get base server URL
  return apiBaseUrl.replace(/\/api\/v1$/, '')
}

/**
 * Convert a relative avatar URL to a complete URL
 * @param avatarUrl - The avatar URL from the backend (could be relative or absolute)
 * @returns Complete URL to access the avatar image
 */
export const getAvatarUrl = (avatarUrl: string | null | undefined): string | null => {
  if (!avatarUrl) {
    return null
  }

  // If it's already a complete URL (http/https) or blob URL, return as is
  if (avatarUrl.startsWith('http://') || avatarUrl.startsWith('https://') || avatarUrl.startsWith('blob:')) {
    return avatarUrl
  }

  // If it's a relative URL starting with /uploads/avatars/, convert to API endpoint
  if (avatarUrl.startsWith('/uploads/avatars/')) {
    const filename = avatarUrl.replace('/uploads/avatars/', '')
    return `${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1'}/avatars/${filename}`
  }

  // If it's a relative URL starting with /uploads/, prepend the base URL
  if (avatarUrl.startsWith('/uploads/')) {
    return getStaticBaseUrl() + avatarUrl
  }

  // If it's a relative URL not starting with /, prepend the base URL and /
  if (!avatarUrl.startsWith('/')) {
    return getStaticBaseUrl() + '/' + avatarUrl
  }

  // For other cases, prepend the base URL
  return getStaticBaseUrl() + avatarUrl
}

/**
 * Get user initials from first name and last name
 * @param firstName - User's first name
 * @param lastName - User's last name
 * @param username - Fallback username
 * @returns User initials (e.g., "JD" for John Doe)
 */
export const getUserInitials = (
  firstName?: string, 
  lastName?: string, 
  username?: string
): string => {
  if (firstName && lastName) {
    return `${firstName[0]}${lastName[0]}`.toUpperCase()
  }
  
  if (firstName) {
    return firstName[0].toUpperCase()
  }
  
  if (username) {
    return username[0].toUpperCase()
  }
  
  return '?'
}
