package com.websocket.chat.config.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@Slf4j
public class StompHandler implements ChannelInterceptor {
//    @Autowired
//    private JwtTokenProviderUtil jwtTokenProviderUtil;
//    @Autowired
//    private UserRepository userRepository;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if (StompCommand.CONNECT == accessor.getCommand()) {
            String jwt = accessor.getFirstNativeHeader("token");
            log.info("Authorization {}", jwt);
//            if (StringUtils.hasText(jwt) && jwt.startsWith("Bearer")) {
//                jwt = jwt.substring(6, jwt.length());
//            }
//            Integer userId = jwtTokenProviderUtil.getUserIdFromAcessToken(jwt);
//
//            if (userId != null) {
//                log.debug("userId: {}", userId);
//                Optional<User> user = userRepository.findById(userId);
//                if (!user.isPresent()) {
//                    throw new RequriedLoginException();
//                } else {
//                    return message;
//                }
//            }
        }
        return message;
    }

    @Override
    public void postSend(Message message, MessageChannel channel, boolean sent) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        String sessionId = accessor.getSessionId();
        switch (accessor.getCommand()) {
            case CONNECT:
                // 유저가 Websocket으로 connect()를 한 뒤 호출됨
                log.info("CONNECT sessionId: {}",sessionId);

                break;
            case DISCONNECT:
                log.info("DISCONNECT");
                log.info("sessionId: {}",sessionId);
                log.info("channel:{}",channel);
                // 유저가 Websocket으로 disconnect() 를 한 뒤 호출됨 or 세션이 끊어졌을 때 발생함(페이지 이동~ 브라우저 닫기 등)
                break;
            default:
                break;
        }

    }
}
