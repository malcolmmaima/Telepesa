import React from 'react'
import { describe, it, expect, vi } from 'vitest'
import { render, screen, fireEvent } from '@testing-library/react'
import { Modal } from '../Modal'

describe('Modal', () => {
  it('renders when isOpen is true', () => {
    render(
      <Modal isOpen={true} onClose={() => {}}>
        <div>Modal Content</div>
      </Modal>
    )

    expect(screen.getByText('Modal Content')).toBeDefined()
  })

  it('does not render when isOpen is false', () => {
    render(
      <Modal isOpen={false} onClose={() => {}}>
        <div>Modal Content</div>
      </Modal>
    )

    expect(screen.queryByText('Modal Content')).toBeNull()
  })

  it('calls onClose when overlay is clicked', () => {
    const onClose = vi.fn()
    render(
      <Modal isOpen={true} onClose={onClose}>
        <div>Modal Content</div>
      </Modal>
    )

    // Click on the backdrop (first div with bg-black class)
    const backdrop = document.querySelector('.bg-black.bg-opacity-50')
    fireEvent.click(backdrop!)

    expect(onClose).toHaveBeenCalledTimes(1)
  })

  it('calls onClose when escape key is pressed', () => {
    const onClose = vi.fn()
    render(
      <Modal isOpen={true} onClose={onClose}>
        <div>Modal Content</div>
      </Modal>
    )

    fireEvent.keyDown(document, { key: 'Escape' })

    expect(onClose).toHaveBeenCalledTimes(1)
  })

  it('does not close when modal content is clicked', () => {
    const onClose = vi.fn()
    render(
      <Modal isOpen={true} onClose={onClose}>
        <div>Modal Content</div>
      </Modal>
    )

    const content = screen.getByText('Modal Content')
    fireEvent.click(content)

    expect(onClose).not.toHaveBeenCalled()
  })

  it('renders with custom title', () => {
    render(
      <Modal isOpen={true} onClose={() => {}} title="Custom Title">
        <div>Modal Content</div>
      </Modal>
    )

    expect(screen.getByText('Custom Title')).toBeDefined()
  })

  it('renders close button when showCloseButton is true', () => {
    render(
      <Modal isOpen={true} onClose={() => {}} showCloseButton={true}>
        <div>Modal Content</div>
      </Modal>
    )

    // Look for the close button by its SVG content
    const closeButton = screen.getByRole('button')
    expect(closeButton).toBeDefined()
  })

  it('applies custom size classes', () => {
    render(
      <Modal isOpen={true} onClose={() => {}} size="xl">
        <div>Modal Content</div>
      </Modal>
    )

    // Check for the xl size class in the modal container
    const modalContainer = document.querySelector('.max-w-4xl')
    expect(modalContainer).toBeTruthy()
  })
})
