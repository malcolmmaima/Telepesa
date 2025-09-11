# WebSocket Real-Time Notifications - Implementation Guide

The frontend now supports real-time notifications via WebSocket connections. Here's what needs to be implemented on the backend:

## WebSocket Endpoint

Create a WebSocket endpoint at `/ws/notifications` that:

1. Accepts authentication via query parameter: `?token=<JWT_TOKEN>&userId=<USER_ID>`
2. Maintains WebSocket connections per user
3. Sends notifications in real-time when they occur

## Message Format

The frontend expects WebSocket messages in this format:

```json
{
  "type": "notification",
  "data": {
    "id": "unique_notification_id",
    "title": "Transaction Completed",
    "message": "Your transfer of KSh 1,000 has been completed successfully.",
    "type": "success", // 'success', 'info', 'warning', 'error'
    "actionUrl": "/transactions/123",
    "actionText": "View Transaction",
    "createdAt": "2024-01-15T10:30:00Z",
    "read": false
  }
}
```

## Other Message Types

1. **Unread Count Update:**
```json
{
  "type": "unread_count_update",
  "data": 5
}
```

2. **Connection Status:**
```json
{
  "type": "connection_status",
  "data": { "connected": true }
}
```

## Frontend Features Implemented

✅ **Real-time WebSocket Connection**: Automatically connects when user logs in
✅ **Toast Notifications**: Shows immediate notifications for new messages
✅ **Browser Notifications**: Native browser notifications (with user permission)
✅ **Unread Count Badge**: Real-time updates in navbar
✅ **Automatic Reconnection**: Handles connection drops with exponential backoff
✅ **Fallback Handling**: Graceful degradation when WebSocket is unavailable

## Backend Implementation Example (Spring Boot)

```java
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new NotificationWebSocketHandler(), "/ws/notifications")
                .setAllowedOrigins("*");
    }
}

@Component
public class NotificationWebSocketHandler extends TextWebSocketHandler {
    
    private final Map<String, WebSocketSession> userSessions = new ConcurrentHashMap<>();
    
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // Extract user ID from query params and authenticate
        String userId = getUserIdFromSession(session);
        userSessions.put(userId, session);
    }
    
    public void sendNotificationToUser(String userId, NotificationMessage notification) {
        WebSocketSession session = userSessions.get(userId);
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(notification)));
            } catch (IOException e) {
                log.error("Failed to send notification to user {}: {}", userId, e.getMessage());
            }
        }
    }
}
```

## Profile Upload Backend Implementation

✅ **Avatar Upload Endpoint**: `POST /api/v1/users/avatar`
✅ **Profile Update Endpoint**: `PUT /api/v1/users/profile` 
✅ **Get Profile Endpoint**: `GET /api/v1/users/profile`
✅ **Change Password Endpoint**: `PUT /api/v1/users/change-password`
✅ **File Storage Service**: Handles image validation and storage
✅ **Database Schema Updates**: Added `avatar_url` and `date_of_birth` fields

The backend profile upload is now fully implemented and the frontend has proper fallback handling for when the endpoint returns 500 errors.

## Testing

To test the real-time notifications:

1. Start the WebSocket server with the notification endpoint
2. Open the customer app and log in
3. Send a test notification via the backend API or admin panel
4. The notification should appear immediately as a toast in the top-right corner
5. The unread count badge should update in the navbar
6. Check that the notification appears in the notifications dropdown
