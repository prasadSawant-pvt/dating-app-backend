package com.example.techiedating.config;

import com.example.techiedating.security.WebSocketAuthInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.HandshakeInterceptor;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable a simple in-memory message broker to carry the messages back to the client
        config.enableSimpleBroker(
            "/topic",  // For subscriptions (broadcasting to multiple clients)
            "/queue"   // For user-specific messages (sending to a specific user)
        );
        
        // Set the application destination prefix - messages whose destination starts with "/app" 
        // will be routed to @MessageMapping methods in @Controller classes
        config.setApplicationDestinationPrefixes("/app");
        
        // Configure user destination prefix for user-specific message delivery
        config.setUserDestinationPrefix("/user");
    }

    @Bean
    public HandshakeInterceptor webSocketAuthInterceptor() {
        return new WebSocketAuthInterceptor(
            null, // This will be autowired by Spring
            null  // This will be autowired by Spring
        );
    }
    
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Register the "/ws" endpoint for WebSocket connections
        registry.addEndpoint("/ws")
                .addInterceptors(webSocketAuthInterceptor())
                .setAllowedOriginPatterns("*")
                .withSockJS() // Enable SockJS fallback options
                .setSuppressCors(true);
        
        // Add an additional endpoint without SockJS for native WebSocket clients
        registry.addEndpoint("/ws")
                .addInterceptors(webSocketAuthInterceptor())
                .setAllowedOriginPatterns("*");
    }
}
