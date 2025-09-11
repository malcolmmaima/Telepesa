import { cn } from '../../lib/utils'
import { useAvatarImage } from '../../hooks/useAvatarImage'

interface AvatarProps {
  src?: string
  alt?: string
  initials?: string
  size?: 'sm' | 'md' | 'lg' | 'xl'
  className?: string
}

const sizes = {
  sm: 'h-8 w-8 text-sm',
  md: 'h-10 w-10 text-base',
  lg: 'h-12 w-12 text-lg',
  xl: 'h-16 w-16 text-xl',
}

export const Avatar = ({ src, alt, initials, size = 'md', className }: AvatarProps) => {
  const { imageSrc, isLoading, error } = useAvatarImage(src)
  
  // Show initials if no src, loading failed, or has error
  if (!src || error || (!imageSrc && !isLoading)) {
    return (
      <div
        className={cn(
          'rounded-full bg-financial-navy text-white flex items-center justify-center font-medium',
          sizes[size],
          className
        )}
        title={error || 'Avatar'}
      >
        {isLoading ? (
          <div className="animate-spin rounded-full h-4/5 w-4/5 border-b-2 border-white"></div>
        ) : (
          initials || '?'
        )}
      </div>
    )
  }

  if (imageSrc) {
    return (
      <img
        src={imageSrc}
        alt={alt || 'Avatar'}
        className={cn('rounded-full object-cover', sizes[size], className)}
      />
    )
  }

  // Loading state
  return (
    <div
      className={cn(
        'rounded-full bg-financial-navy text-white flex items-center justify-center font-medium',
        sizes[size],
        className
      )}
    >
      <div className="animate-spin rounded-full h-4/5 w-4/5 border-b-2 border-white"></div>
    </div>
  )
}
