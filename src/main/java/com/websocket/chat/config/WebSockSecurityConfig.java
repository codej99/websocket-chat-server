package com.websocket.chat.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.messaging.MessageSecurityMetadataSourceRegistry;
import org.springframework.security.config.annotation.web.socket.AbstractSecurityWebSocketMessageBrokerConfigurer;

/**
 * Websocket security 설정
 */
//@Configuration
public class WebSockSecurityConfig extends AbstractSecurityWebSocketMessageBrokerConfigurer {
//    @Override
//    protected void configureInbound(MessageSecurityMetadataSourceRegistry messages) {
//        messages
//                .simpDestMatchers("/ws-stomp/**", "/pub/**").hasRole("USER") // websocket 연결, 메시지 발행에 대하여 User 권한이 있어야 함을 설정.
//                .anyMessage().permitAll();
//    }
//
//    @Override
//    protected boolean sameOriginDisabled() {
//        return true;
//    }
}
