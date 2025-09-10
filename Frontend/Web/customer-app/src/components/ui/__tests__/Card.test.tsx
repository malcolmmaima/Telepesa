import { render, screen } from '@testing-library/react'
import { describe, it, expect } from 'vitest'
import { Card, CardHeader, CardBody, CardFooter } from '../Card'

describe('Card', () => {
  it('renders with basic content', () => {
    render(
      <Card>
        <p>Card content</p>
      </Card>
    )

    expect(screen.getByText('Card content')).toBeInTheDocument()
  })

  it('renders with title and description', () => {
    render(
      <Card title="Test Card" description="This is a test card">
        <p>Card content</p>
      </Card>
    )

    expect(screen.getByText('Test Card')).toBeInTheDocument()
    expect(screen.getByText('This is a test card')).toBeInTheDocument()
    expect(screen.getByText('Card content')).toBeInTheDocument()
  })

  it('renders with actions', () => {
    const actions = <button type="button">Action</button>

    render(
      <Card title="Card with Actions" actions={actions}>
        <p>Card content</p>
      </Card>
    )

    expect(screen.getByText('Card with Actions')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Action' })).toBeInTheDocument()
  })

  it('applies custom className', () => {
    render(
      <Card className="custom-card">
        <p>Content</p>
      </Card>
    )

    const cardElement = screen.getByText('Content').closest('div')
    expect(cardElement).toHaveClass('custom-card')
    expect(cardElement).toHaveClass('card') // Should also have default class
  })

  it('controls padding with padding prop', () => {
    const { rerender } = render(
      <Card padding={false}>
        <p>No padding content</p>
      </Card>
    )

    let cardBody = screen.getByText('No padding content').closest('div')
    expect(cardBody).toHaveClass('p-0')

    rerender(
      <Card padding={true}>
        <p>With padding content</p>
      </Card>
    )

    cardBody = screen.getByText('With padding content').closest('div')
    expect(cardBody).toHaveClass('card-body')
  })
})

describe('CardHeader', () => {
  it('renders header content', () => {
    render(
      <CardHeader>
        <h2>Header Content</h2>
      </CardHeader>
    )

    expect(screen.getByText('Header Content')).toBeInTheDocument()

    const headerElement = screen.getByText('Header Content').closest('div')
    expect(headerElement).toHaveClass('card-header')
  })

  it('applies custom className', () => {
    render(
      <CardHeader className="custom-header">
        <h2>Header</h2>
      </CardHeader>
    )

    const headerElement = screen.getByText('Header').closest('div')
    expect(headerElement).toHaveClass('custom-header')
    expect(headerElement).toHaveClass('card-header')
  })
})

describe('CardBody', () => {
  it('renders body content', () => {
    render(
      <CardBody>
        <p>Body Content</p>
      </CardBody>
    )

    expect(screen.getByText('Body Content')).toBeInTheDocument()

    const bodyElement = screen.getByText('Body Content').closest('div')
    expect(bodyElement).toHaveClass('card-body')
  })

  it('applies custom className', () => {
    render(
      <CardBody className="custom-body">
        <p>Body</p>
      </CardBody>
    )

    const bodyElement = screen.getByText('Body').closest('div')
    expect(bodyElement).toHaveClass('custom-body')
    expect(bodyElement).toHaveClass('card-body')
  })
})

describe('CardFooter', () => {
  it('renders footer content', () => {
    render(
      <CardFooter>
        <button type="button">Footer Button</button>
      </CardFooter>
    )

    expect(screen.getByRole('button', { name: 'Footer Button' })).toBeInTheDocument()

    const footerElement = screen.getByRole('button').closest('div')
    expect(footerElement).toHaveClass('px-6', 'py-4', 'border-t', 'border-gray-100')
  })

  it('applies custom className', () => {
    render(
      <CardFooter className="custom-footer">
        <p>Footer</p>
      </CardFooter>
    )

    const footerElement = screen.getByText('Footer').closest('div')
    expect(footerElement).toHaveClass('custom-footer')
    expect(footerElement).toHaveClass('px-6', 'py-4', 'border-t', 'border-gray-100')
  })
})
