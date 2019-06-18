package com.websocket.chat.config.handler;

import com.websocket.chat.model.ChatMessage;
import com.websocket.chat.model.ChatRoom;
import com.websocket.chat.service.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.security.Principal;

@Slf4j
@RequiredArgsConstructor
@Component
public class StompHandler implements ChannelInterceptor {

    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTemplate redisTemplate;
    private final ChannelTopic channelTopic;

    private HashOperations hashOps;
    private ValueOperations valueOps;

    @PostConstruct
    private void init() {
        hashOps = redisTemplate.opsForHash();
        valueOps = redisTemplate.opsForValue();
    }

    // websocket을 통해 들어온 요청이 처리 되기전 실행된다.
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        // websocket 연결시 헤더의 jwt token 검증
        if (StompCommand.CONNECT == accessor.getCommand()) {
            jwtTokenProvider.getUserNameFromJwt(accessor.getFirstNativeHeader("token"));
        } else if (StompCommand.SUBSCRIBE == accessor.getCommand()) {
            String roomId = getRoomId((String) message.getHeaders().get("simpDestination"));
            String sessionId = (String) message.getHeaders().get("simpSessionId");
            Long userCnt = valueOps.increment(ChatRoom.USER_COUNT + roomId);
            hashOps.put(ChatRoom.USER_ENTRY, sessionId, roomId);

            Principal principal = (Principal) message.getHeaders().get("simpUser");
            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setRoomId(roomId);
            chatMessage.setType(ChatMessage.MessageType.ENTER);
            chatMessage.setSender("[알림]");
            chatMessage.setMessage(principal.getName() + "님이 방에 입장했습니다.");
            redisTemplate.convertAndSend(channelTopic.getTopic(), chatMessage);

            log.info("SUBSCRIBED {} = {}", roomId, userCnt);
        } else if (StompCommand.DISCONNECT == accessor.getCommand()) {
            String sessionId = (String) message.getHeaders().get("simpSessionId");
            String roomId = (String) hashOps.get(ChatRoom.USER_ENTRY, sessionId);
            Long userCnt = valueOps.decrement(ChatRoom.USER_COUNT + roomId);
            log.info("DISCONNECTED {} = {}", roomId, userCnt);

            Principal principal = (Principal) message.getHeaders().get("simpUser");
            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setRoomId(roomId);
            chatMessage.setType(ChatMessage.MessageType.QUIT);
            chatMessage.setSender("[알림]");
            chatMessage.setMessage(principal.getName() + "님이 방에서 나갔습니다.");
            log.info("DISCONNECTED {} = {}", channelTopic.getTopic(), chatMessage);
            redisTemplate.convertAndSend(channelTopic.getTopic(), chatMessage);
        }
        return message;
    }

    // websocket을 통해 들어온 요청이 처리된 후에 실행된다.
//    @Override
//    public void postSend(Message message, MessageChannel channel, boolean sent) {
//        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
//        if (StompCommand.SUBSCRIBE == accessor.getCommand()) {
//            String roomId = (String) message.getHeaders().get("simpDestination");
//            String sessionId = (String) message.getHeaders().get("simpSessionId");
//            Long userCnt = valueOps.increment(CHAT_USER_COUNT + roomId);
//            hashOps.put(CHAT_ROOM_ENTRY, sessionId, roomId);
//            log.info("SUBSCRIBED {} = {}", roomId, userCnt);
//        } else if (StompCommand.DISCONNECT == accessor.getCommand()) {
//            String sessionId = (String) message.getHeaders().get("simpSessionId");
//            String roomId = hashOps.get(CHAT_ROOM_ENTRY, sessionId);
//            Long userCnt = valueOps.decrement(CHAT_USER_COUNT + roomId);
//            log.info("DISCONNECTED {} = {}", roomId, userCnt);
//        }
//    }

    private String getRoomId(String destination) {
        int lastIndex = destination.lastIndexOf("/");
        if (lastIndex != -1)
            return destination.substring(lastIndex + 1);
        else
            return "";
    }
}
