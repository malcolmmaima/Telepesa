import { useState, useEffect } from 'react'
import { useAuth } from '../store/auth'

/**
 * Hook for loading avatar images with authentication
 * Converts authenticated image requests to blob URLs for display
 */
export const useAvatarImage = (avatarUrl: string | null | undefined) => {
  const [imageSrc, setImageSrc] = useState<string | null>(null)
  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const { accessToken } = useAuth()

  useEffect(() => {
    // Clear previous state
    setImageSrc(null)
    setError(null)
    
    if (!avatarUrl) {
      return
    }

    // If it's already a blob URL or external URL, use it directly
    if (avatarUrl.startsWith('blob:') || avatarUrl.startsWith('http://') || avatarUrl.startsWith('https://')) {
      setImageSrc(avatarUrl)
      return
    }

    // If no token available, can only fetch static files
    if (!accessToken && !avatarUrl.startsWith('/uploads/')) {
      setError('No authentication token available')
      return
    }

    const fetchImage = async () => {
      setIsLoading(true)
      setError(null)

      try {
        // For static files (uploads), access them directly without authentication
        // Since we've configured the backend to allow public access to /uploads/**
        let fetchUrl = avatarUrl

        // Convert relative URLs to full URLs through user service
        if (avatarUrl.startsWith('/uploads/')) {
          // Access static files directly through user service (port 8081)
          fetchUrl = `http://localhost:8081${avatarUrl}`
        }

        // Prepare headers - only add auth for non-static files
        const headers: HeadersInit = {}
        
        // Only add authentication header for non-static files
        if (!avatarUrl.startsWith('/uploads/')) {
          headers['Authorization'] = `Bearer ${accessToken}`
        }
        
        const response = await fetch(fetchUrl, {
          headers,
        })

        if (!response.ok) {
          if (response.status === 401) {
            throw new Error('Authentication required to load image')
          } else if (response.status === 404) {
            throw new Error('Image not found')
          } else {
            throw new Error(`Failed to load image: ${response.statusText}`)
          }
        }

        const blob = await response.blob()
        const blobUrl = URL.createObjectURL(blob)
        setImageSrc(blobUrl)
      } catch (err) {
        console.error('Failed to load avatar image:', err)
        setError(err instanceof Error ? err.message : 'Failed to load image')
      } finally {
        setIsLoading(false)
      }
    }

    fetchImage()

    // Cleanup function to revoke blob URL
    return () => {
      if (imageSrc && imageSrc.startsWith('blob:')) {
        URL.revokeObjectURL(imageSrc)
      }
    }
  }, [avatarUrl, accessToken])

  // Cleanup blob URL on unmount
  useEffect(() => {
    return () => {
      if (imageSrc && imageSrc.startsWith('blob:')) {
        URL.revokeObjectURL(imageSrc)
      }
    }
  }, [imageSrc])

  return {
    imageSrc,
    isLoading,
    error,
  }
}
