import { cn } from '../../lib/utils'
import type { CardProps } from '../../types'

export const Card = ({
  className,
  title,
  description,
  actions,
  padding = true,
  children,
}: CardProps) => {
  return (
    <div className={cn('card', className)}>
      {(title || description || actions) && (
        <div className="card-header">
          <div className="flex items-center justify-between">
            <div>
              {title && <h3 className="text-lg font-semibold text-financial-navy">{title}</h3>}
              {description && <p className="text-sm text-financial-gray mt-1">{description}</p>}
            </div>
            {actions && <div className="flex items-center space-x-2">{actions}</div>}
          </div>
        </div>
      )}
      <div className={cn(padding && 'card-body', !padding && 'p-0')}>{children}</div>
    </div>
  )
}

export const CardHeader = ({ className, children }: CardProps) => {
  return <div className={cn('card-header', className)}>{children}</div>
}

export const CardBody = ({ className, children }: CardProps) => {
  return <div className={cn('card-body', className)}>{children}</div>
}

export const CardFooter = ({ className, children }: CardProps) => {
  return <div className={cn('px-6 py-4 border-t border-gray-100', className)}>{children}</div>
}
