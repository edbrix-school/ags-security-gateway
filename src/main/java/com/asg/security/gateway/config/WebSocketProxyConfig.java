package com.asg.security.gateway.config;

import com.asg.security.gateway.handler.WebSocketProxyHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketProxyConfig implements WebSocketConfigurer {

    private final WebSocketProxyHandler webSocketProxyHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(
                        webSocketProxyHandler,
                        "/asg/finance/api/ws",
                        "/asg/finance/api/ws/**",
                        "/asg/finance/api/websocket",
                        "/asg/finance/api/websocket/**"
                )
                .setAllowedOriginPatterns("*");
    }
}
