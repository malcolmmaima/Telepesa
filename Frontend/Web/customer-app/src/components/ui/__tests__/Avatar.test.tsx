import React from 'react'
import { render, screen } from '@testing-library/react'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { Avatar } from '../Avatar'

// Mock the useAvatarImage hook
vi.mock('../../../hooks/useAvatarImage', () => ({
  useAvatarImage: vi.fn(),
}))

// Import the mocked hook
import { useAvatarImage } from '../../../hooks/useAvatarImage'
const mockUseAvatarImage = vi.mocked(useAvatarImage)

describe('Avatar', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('renders avatar image when src is provided', () => {
    mockUseAvatarImage.mockReturnValue({
      imageSrc: 'https://example.com/avatar.jpg',
      isLoading: false,
      error: null,
    })

    render(<Avatar src="https://example.com/avatar.jpg" alt="John Doe" />)

    const img = screen.getByRole('img')
    expect(img).toHaveAttribute('src', 'https://example.com/avatar.jpg')
    expect(img).toHaveAttribute('alt', 'John Doe')
  })

  it('renders fallback when no image is available', () => {
    mockUseAvatarImage.mockReturnValue({
      imageSrc: null,
      isLoading: false,
      error: null,
    })

    render(<Avatar initials="JD" />)

    expect(screen.getByText('JD')).toBeDefined()
  })

  it('renders different sizes correctly', () => {
    mockUseAvatarImage.mockReturnValue({
      imageSrc: null,
      isLoading: false,
      error: null,
    })

    const { container: container1 } = render(<Avatar initials="JD" size="sm" />)
    expect(container1.querySelector('.h-8.w-8')).toBeTruthy()

    const { container: container2 } = render(<Avatar initials="JD" size="md" />)
    expect(container2.querySelector('.h-10.w-10')).toBeTruthy()

    const { container: container3 } = render(<Avatar initials="JD" size="lg" />)
    expect(container3.querySelector('.h-12.w-12')).toBeTruthy()
  })

  it('handles image loading error', () => {
    mockUseAvatarImage.mockReturnValue({
      imageSrc: null,
      isLoading: false,
      error: 'Failed to load image',
    })

    render(<Avatar src="invalid-url" initials="JD" />)

    const fallback = screen.getByText('JD')
    expect(fallback).toBeDefined()
    expect(fallback.closest('div')).toHaveAttribute('title', 'Failed to load image')
  })

  it('applies custom className', () => {
    mockUseAvatarImage.mockReturnValue({
      imageSrc: null,
      isLoading: false,
      error: null,
    })

    const { container } = render(<Avatar initials="JD" className="custom-class" />)
    expect(container.querySelector('.custom-class')).toBeTruthy()
  })

  it('renders loading state', () => {
    mockUseAvatarImage.mockReturnValue({
      imageSrc: null,
      isLoading: true,
      error: null,
    })

    const { container } = render(<Avatar src="loading-url" />)

    const loadingSpinner = container.querySelector('.animate-spin')
    expect(loadingSpinner).toBeTruthy()
  })
})
