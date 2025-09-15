package com.maelcolium.telepesa.notification.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
@RequiredArgsConstructor
public class NotificationWebSocketHandler implements WebSocketHandler {

    private final ObjectMapper objectMapper;
    private final Map<String, WebSocketSession> userSessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("WebSocket connection established: {}", session.getId());
        
        // Extract userId from query parameters
        URI uri = session.getUri();
        if (uri != null) {
            String query = uri.getQuery();
            if (query != null && query.contains("userId=")) {
                String userId = UriComponentsBuilder.fromUriString("?" + query)
                        .build()
                        .getQueryParams()
                        .getFirst("userId");
                
                if (userId != null) {
                    userSessions.put(userId, session);
                    log.info("User {} connected via WebSocket", userId);
                    
                    // Send connection confirmation
                    sendMessage(session, Map.of(
                        "type", "connection",
                        "status", "connected",
                        "userId", userId,
                        "timestamp", System.currentTimeMillis()
                    ));
                }
            }
        }
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        log.info("Received WebSocket message: {}", message.getPayload());
        
        // Handle ping/pong or other client messages
        if (message instanceof TextMessage) {
            String payload = ((TextMessage) message).getPayload();
            if ("ping".equals(payload)) {
                sendMessage(session, Map.of("type", "pong", "timestamp", System.currentTimeMillis()));
            }
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("WebSocket transport error for session {}: {}", session.getId(), exception.getMessage());
        removeSession(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        log.info("WebSocket connection closed: {} with status: {}", session.getId(), closeStatus);
        removeSession(session);
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    private void removeSession(WebSocketSession session) {
        userSessions.entrySet().removeIf(entry -> entry.getValue().equals(session));
    }

    public void sendNotificationToUser(String userId, Object notification) {
        WebSocketSession session = userSessions.get(userId);
        if (session != null && session.isOpen()) {
            try {
                Map<String, Object> message = Map.of(
                    "type", "notification",
                    "data", notification,
                    "timestamp", System.currentTimeMillis()
                );
                sendMessage(session, message);
                log.info("Sent notification to user {} via WebSocket", userId);
            } catch (Exception e) {
                log.error("Failed to send notification to user {} via WebSocket: {}", userId, e.getMessage());
                userSessions.remove(userId);
            }
        } else {
            log.debug("No active WebSocket session for user {}", userId);
        }
    }

    private void sendMessage(WebSocketSession session, Object message) throws IOException {
        if (session.isOpen()) {
            String json = objectMapper.writeValueAsString(message);
            session.sendMessage(new TextMessage(json));
        }
    }

    public int getActiveConnectionsCount() {
        return userSessions.size();
    }
}
