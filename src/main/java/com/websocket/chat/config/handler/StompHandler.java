package com.websocket.chat.config.handler;

import com.websocket.chat.service.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

@Slf4j
@RequiredArgsConstructor
@Component
public class StompHandler implements ChannelInterceptor {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
//        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
//        if (StompCommand.CONNECT == accessor.getCommand()) {
//            String jwt = accessor.getFirstNativeHeader("token");
//            log.info("Authorization {}", jwt);
//            String nickname = "";
//            if (StringUtils.hasText(jwt)) {
//                try {
//                    nickname = jwtTokenProvider.validateToken(jwt);
//                } catch(Exception e) {
//
//                    //final StompHeaderAccessor accessor = StompHeaderAccessor.create(accessor.getCommand());
//                    accessor.setSessionId(accessor.getSessionId());
//                    @SuppressWarnings("unchecked")
//                    final MultiValueMap<String, String> nativeHeaders = (MultiValueMap<String, String>) accessor.getHeader(StompHeaderAccessor.NATIVE_HEADERS);
//                    accessor.addNativeHeaders(nativeHeaders);
//
//                    // add custom headers
//                    accessor.addNativeHeader("CUSTOM01", "CUSTOM01");
//
//                    final Message<?> newMessage = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
//                    return newMessage;
//                }
//            }
//        }
//        return message;

        //////////////////////////

//        log.info("Outbound channel pre send ...");
//        final StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(message);
//        final StompCommand command = headerAccessor.getCommand();
//        if (command != null) {
//            switch (command) {
//                case CONNECT:
//                    final StompHeaderAccessor accessor = StompHeaderAccessor.create(headerAccessor.getCommand());
//                    accessor.setSessionId(headerAccessor.getSessionId());
//                    @SuppressWarnings("unchecked")
//                    final MultiValueMap<String, String> nativeHeaders = (MultiValueMap<String, String>) headerAccessor.getHeader(StompHeaderAccessor.NATIVE_HEADERS);
//                    accessor.addNativeHeaders(nativeHeaders);
//
//                    // add custom headers
//                    accessor.addNativeHeader("CUSTOM01", "CUSTOM01");
//
//                    return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
//                default:
//                    break;
//            }
//        }
        return message;
    }

//    @Override
//    public void postSend(Message message, MessageChannel channel, boolean sent) {
//        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
//        String sessionId = accessor.getSessionId();
//        switch (accessor.getCommand()) {
//            case CONNECT:
//                // 유저가 Websocket으로 connect()를 한 뒤 호출됨
//                log.info("CONNECT sessionId: {}", sessionId);
//
//                break;
//            case DISCONNECT:
//                log.info("DISCONNECT");
//                log.info("sessionId: {}", sessionId);
//                log.info("channel:{}", channel);
//                // 유저가 Websocket으로 disconnect() 를 한 뒤 호출됨 or 세션이 끊어졌을 때 발생함(페이지 이동~ 브라우저 닫기 등)
//                break;
//            default:
//                break;
//        }
//
//    }
}
