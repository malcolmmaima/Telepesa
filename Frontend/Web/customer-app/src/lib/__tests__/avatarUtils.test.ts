import { describe, it, expect } from 'vitest'
import { getAvatarUrl, getUserInitials } from '../avatarUtils'

describe('avatarUtils', () => {
  describe('getAvatarUrl', () => {
    it('returns null for empty/undefined input', () => {
      expect(getAvatarUrl('')).toBeNull()
      expect(getAvatarUrl(null)).toBeNull()
      expect(getAvatarUrl(undefined)).toBeNull()
    })

    it('returns absolute URLs as-is', () => {
      const httpUrl = 'http://example.com/avatar.jpg'
      const httpsUrl = 'https://example.com/avatar.jpg'
      const blobUrl = 'blob:http://localhost/avatar'

      expect(getAvatarUrl(httpUrl)).toBe(httpUrl)
      expect(getAvatarUrl(httpsUrl)).toBe(httpsUrl)
      expect(getAvatarUrl(blobUrl)).toBe(blobUrl)
    })

    it('converts relative avatar URLs to API endpoints', () => {
      const relativeUrl = '/uploads/avatars/avatar123.jpg'
      const result = getAvatarUrl(relativeUrl)
      expect(result).toContain('/api/v1/avatars/avatar123.jpg')
    })

    it('handles relative upload URLs', () => {
      const uploadUrl = '/uploads/profile.jpg'
      const result = getAvatarUrl(uploadUrl)
      expect(result).toContain('/uploads/profile.jpg')
    })
  })

  describe('getUserInitials', () => {
    it('gets initials from first and last name', () => {
      expect(getUserInitials('John', 'Doe')).toBe('JD')
      expect(getUserInitials('Jane', 'Smith')).toBe('JS')
    })

    it('gets initial from first name only', () => {
      expect(getUserInitials('John')).toBe('J')
    })

    it('uses username as fallback', () => {
      expect(getUserInitials(undefined, undefined, 'johndoe')).toBe('J')
    })

    it('handles empty parameters', () => {
      expect(getUserInitials()).toBe('?')
      expect(getUserInitials('', '')).toBe('?')
    })

    it('converts to uppercase', () => {
      expect(getUserInitials('john', 'doe')).toBe('JD')
      expect(getUserInitials('jane')).toBe('J')
    })

    it('handles single character names', () => {
      expect(getUserInitials('A', 'B')).toBe('AB')
      expect(getUserInitials('X')).toBe('X')
    })
  })
})
