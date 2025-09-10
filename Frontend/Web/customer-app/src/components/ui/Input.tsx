import { forwardRef, useId } from 'react'
import { cn } from '../../lib/utils'
import type { InputProps } from '../../types'

export const Input = forwardRef<HTMLInputElement, InputProps>(
  ({ className, label, placeholder, type = 'text', error, required, disabled, id, ...props }, ref) => {
    const inputId = useId()
    const finalId = id || inputId

    return (
      <div className="space-y-2">
        {label && (
          <label htmlFor={finalId} className="block text-sm font-medium text-financial-navy">
            {label}
            {required && <span className="ml-1 text-financial-danger">*</span>}
          </label>
        )}
        <input
          id={finalId}
          type={type}
          className={cn(
            'input',
            error &&
              'border-financial-danger focus:ring-financial-danger focus:border-financial-danger',
            disabled && 'bg-gray-50 cursor-not-allowed',
            className
          )}
          placeholder={placeholder}
          disabled={disabled}
          ref={ref}
          {...props}
        />
        {error && (
          <p className="text-sm text-financial-danger flex items-center">
            <svg
              className="mr-1 h-4 w-4"
              fill="currentColor"
              viewBox="0 0 20 20"
              xmlns="http://www.w3.org/2000/svg"
            >
              <path
                clipRule="evenodd"
                d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7 4a1 1 0 11-2 0 1 1 0 012 0zm-1-9a1 1 0 00-1 1v4a1 1 0 102 0V6a1 1 0 00-1-1z"
                fillRule="evenodd"
              />
            </svg>
            {error}
          </p>
        )}
      </div>
    )
  }
)

Input.displayName = 'Input'

export const TextArea = forwardRef<
  HTMLTextAreaElement,
  Omit<InputProps, 'onChange' | 'onBlur'> & {
    onChange?: (event: React.ChangeEvent<HTMLTextAreaElement>) => void
    onBlur?: (event: React.FocusEvent<HTMLTextAreaElement>) => void
  }
>(({ className, label, placeholder, error, required, disabled, id, ...props }, ref) => {
  const inputId = useId()
  const finalId = id || inputId

  return (
    <div className="space-y-2">
      {label && (
        <label htmlFor={finalId} className="block text-sm font-medium text-financial-navy">
          {label}
          {required && <span className="ml-1 text-financial-danger">*</span>}
        </label>
      )}
      <textarea
        id={finalId}
        className={cn(
          'input min-h-[80px] resize-y',
          error &&
            'border-financial-danger focus:ring-financial-danger focus:border-financial-danger',
          disabled && 'bg-gray-50 cursor-not-allowed',
          className
        )}
        placeholder={placeholder}
        disabled={disabled}
        ref={ref}
        {...props}
      />
      {error && (
        <p className="text-sm text-financial-danger flex items-center">
          <svg
            className="mr-1 h-4 w-4"
            fill="currentColor"
            viewBox="0 0 20 20"
            xmlns="http://www.w3.org/2000/svg"
          >
            <path
              clipRule="evenodd"
              d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7 4a1 1 0 11-2 0 1 1 0 012 0zm-1-9a1 1 0 00-1 1v4a1 1 0 102 0V6a1 1 0 00-1-1z"
              fillRule="evenodd"
            />
          </svg>
          {error}
        </p>
      )}
    </div>
  )
})

TextArea.displayName = 'TextArea'
