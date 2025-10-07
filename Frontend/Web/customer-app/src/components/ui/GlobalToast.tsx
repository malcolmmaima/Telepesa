import React from 'react'
import { useToast } from '../../store/toast'
import { NotificationToast } from './NotificationToast'

export function GlobalToast() {
  const { current, clear } = useToast()
  return (
    <NotificationToast
      notification={
        current
          ? {
              id: 0,
              userId: 0,
              questionId: 0,
              question: '',
              answer: '',
              createdAt: '',
              // Map our toast structure into NotificationToast props shape
              // We only use type/title/message/action fields there
              // @ts-ignore - using Notification type loosely for rendering
              type: current.type,
              title: current.title,
              message: current.message,
              actionText: current.actionText,
              actionUrl: current.actionUrl,
            }
          : null
      }
      onClose={clear}
      duration={current?.duration ?? 5000}
    />
  )
}
