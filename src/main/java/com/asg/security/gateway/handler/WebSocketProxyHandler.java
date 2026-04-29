package com.asg.security.gateway.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.SubProtocolCapable;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import java.net.URI;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class WebSocketProxyHandler extends AbstractWebSocketHandler implements SubProtocolCapable {

    private static final List<String> STOMP_SUB_PROTOCOLS = List.of("v12.stomp", "v11.stomp", "v10.stomp");

    @Value("${finance.ws.url:ws://localhost:8086/finance/api}")
    private String financeWsBaseUrl;

    private final ConcurrentHashMap<String, WebSocketSession> backendSessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession clientSession) throws Exception {
        String path = extractBackendPath(clientSession);
        URI backendUri = URI.create(financeWsBaseUrl + path);
        log.info("Proxying WebSocket connection to: {}", backendUri);

        StandardWebSocketClient client = new StandardWebSocketClient();
        WebSocketHttpHeaders headers = new WebSocketHttpHeaders();
        HttpHeaders clientHeaders = clientSession.getHandshakeHeaders();
        copyHeaderIfPresent(clientHeaders, headers, HttpHeaders.ORIGIN);
        copyHeaderIfPresent(clientHeaders, headers, HttpHeaders.AUTHORIZATION);
        copyHeaderIfPresent(clientHeaders, headers, HttpHeaders.COOKIE);
        copyHeaderIfPresent(clientHeaders, headers, "X-Company-Poid");


        WebSocketSession backendSession = client.execute(new AbstractWebSocketHandler() {
            @Override
            protected void handleTextMessage(WebSocketSession backendSession, TextMessage message) throws Exception {
                log.debug("Backend -> client text frame: {}", summarize(message));
                if (clientSession.isOpen()) {
                    clientSession.sendMessage(new TextMessage(message.asBytes()));
                }
            }

            @Override
            protected void handleBinaryMessage(WebSocketSession backendSession, BinaryMessage message) throws Exception {
                log.debug("Backend -> client binary frame: {} bytes", message.getPayloadLength());
                if (clientSession.isOpen()) {
                    clientSession.sendMessage(message);
                }
            }

            @Override
            public void handleTransportError(WebSocketSession session, Throwable exception) {
                log.error("Backend transport error for client session {}: {}", clientSession.getId(), exception.getMessage(), exception);
            }

            @Override
            public void afterConnectionClosed(WebSocketSession backendSession, CloseStatus status) {
                log.warn("Backend WebSocket closed for client session {} with status {}", clientSession.getId(), status);
                backendSessions.remove(clientSession.getId());
                try {
                    if (clientSession.isOpen()) {
                        clientSession.close(status);
                    }
                } catch (Exception e) {
                    log.warn("Error closing client session: {}", e.getMessage());
                }
            }
        }, headers, backendUri).get(10, TimeUnit.SECONDS);

        backendSessions.put(clientSession.getId(), backendSession);
        log.info("Backend WebSocket connected for client session {}", clientSession.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession clientSession, TextMessage message) throws Exception {
        log.debug("Client -> backend text frame: {}", summarize(message));
        WebSocketSession backendSession = backendSessions.get(clientSession.getId());
        if (backendSession != null && backendSession.isOpen()) {
            backendSession.sendMessage(new TextMessage(message.asBytes()));
        }
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession clientSession, BinaryMessage message) throws Exception {
        log.debug("Client -> backend binary frame: {} bytes", message.getPayloadLength());
        WebSocketSession backendSession = backendSessions.get(clientSession.getId());
        if (backendSession != null && backendSession.isOpen()) {
            backendSession.sendMessage(message);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.error("Client transport error for session {}: {}", session.getId(), exception.getMessage(), exception);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession clientSession, CloseStatus status) {
        log.warn("Client WebSocket closed for session {} with status {}", clientSession.getId(), status);
        WebSocketSession backendSession = backendSessions.remove(clientSession.getId());
        if (backendSession != null && backendSession.isOpen()) {
            try { backendSession.close(status); } catch (Exception e) {
                log.warn("Error closing backend session: {}", e.getMessage());
            }
        }
    }

    private String extractBackendPath(WebSocketSession session) {
        String uri = session.getUri() != null ? session.getUri().toString() : "";
        // Strip the gateway prefix: /asg/finance/api → keep /ws/...
        int idx = uri.indexOf("/asg/finance/api");
        if (idx >= 0) {
            return uri.substring(idx + "/asg/finance/api".length());
        }
        return "/ws";
    }

    private void copyHeaderIfPresent(HttpHeaders source, WebSocketHttpHeaders target, String headerName) {
        String value = source.getFirst(headerName);
        if (value != null && !value.isBlank()) {
            target.add(headerName, value);
        }
    }

    @Override
    public List<String> getSubProtocols() {
        return STOMP_SUB_PROTOCOLS;
    }

    private String summarize(TextMessage message) {
        String payload = message.getPayload()
                .replace("\0", "\\0")
                .replace("\r", "\\r")
                .replace("\n", "\\n");
        return payload.length() > 200 ? payload.substring(0, 200) + "..." : payload;
    }
}
