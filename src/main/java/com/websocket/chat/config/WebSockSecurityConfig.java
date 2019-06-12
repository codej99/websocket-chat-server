package com.websocket.chat.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.messaging.MessageSecurityMetadataSourceRegistry;
import org.springframework.security.config.annotation.web.socket.AbstractSecurityWebSocketMessageBrokerConfigurer;

/**
 * Websocket security 설정
 */
@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE + 50)
public class WebSockSecurityConfig extends AbstractSecurityWebSocketMessageBrokerConfigurer {
    @Override
    protected void configureInbound(MessageSecurityMetadataSourceRegistry messages) {
        // websocket 연결, 메시지 발행에 대하여 User 권한이 있어야 함을 명시한다.
        messages
                .simpDestMatchers("/ws-stomp/**", "/pub/**").hasRole("USER")
                .anyMessage().permitAll();
    }

    @Override
    protected boolean sameOriginDisabled() {
        return true;
    }
}
