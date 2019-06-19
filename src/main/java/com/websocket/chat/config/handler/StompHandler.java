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
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Component
public class StompHandler implements ChannelInterceptor {

    private final JwtTokenProvider jwtTokenProvider;
    private final ChannelTopic channelTopic;
    private final RedisTemplate redisTemplate;

    private HashOperations<String, String, String> hashOps;
    private ValueOperations<String, Long> valueOps;

    @PostConstruct
    private void init() {
        hashOps = redisTemplate.opsForHash();
        valueOps = redisTemplate.opsForValue();
    }

    // websocket을 통해 들어온 요청이 처리 되기전 실행된다.
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        if (StompCommand.CONNECT == accessor.getCommand()) { // websocket 연결요청
            String jwtToken = accessor.getFirstNativeHeader("token");
            log.info("CONNECT {}", jwtToken);
            // Header의 jwt token 검증
            jwtTokenProvider.validateToken(jwtToken);
        } else if (StompCommand.SUBSCRIBE == accessor.getCommand()) { // 채팅룸 구독요청
            // header정보에서 구독 destination정보를 얻고, roomId를 추출한다.
            String roomId = getRoomId(Optional.ofNullable((String) message.getHeaders().get("simpDestination")).orElse("InvalidRoomId"));
            // 채팅방에 들어온 클라이언트 sessionId를 roomId와 맵핑해 놓는다.(나중에 특정 세션이 어떤 채팅방에 들어가 있는지 알기 위함)
            String sessionId = (String) message.getHeaders().get("simpSessionId");
            hashOps.put(ChatRoom.USER_ENTRY, sessionId, roomId);
            // 채팅방의 인원수를 +1한다.
            long userCount = Optional.ofNullable(valueOps.increment(ChatRoom.USER_COUNT + roomId)).orElse(0L);
            // 클라이언트 입장 메시지를 채팅방에 발송한다.(redis publish)
            String name = Optional.ofNullable((Principal) message.getHeaders().get("simpUser")).map(Principal::getName).orElse("UnknownUser");
            sendInfoMessage(ChatMessage.MessageType.ENTER, roomId, name, userCount);
            log.info("SUBSCRIBED {}, {}", name, roomId);
        } else if (StompCommand.DISCONNECT == accessor.getCommand()) { // Websocket 연결 종료
            // 연결이 종료된 클라이언트 sesssionId로 채팅방 id를 얻는다.
            String sessionId = (String) message.getHeaders().get("simpSessionId");
            String roomId = hashOps.get(ChatRoom.USER_ENTRY, sessionId);
            // 채팅방의 인원수를 -1한다.
            long userCount = Optional.ofNullable(valueOps.decrement(ChatRoom.USER_COUNT + roomId)).filter(count -> count > 0).orElse(0L);
            // 클라이언트 입장 메시지를 채팅방에 발송한다.(redis publish)
            String name = Optional.ofNullable((Principal) message.getHeaders().get("simpUser")).map(Principal::getName).orElse("UnknownUser");
            sendInfoMessage(ChatMessage.MessageType.QUIT, roomId, name, userCount);
            // 퇴장한 클라이언트의 입장 정보를 삭제한다.
            hashOps.delete(ChatRoom.USER_ENTRY, sessionId);
            log.info("DISCONNECTED {}, {}", channelTopic.getTopic(), roomId);
        }
        return message;
    }

    /**
     * destination정보에서 roomId 추출
     */
    private String getRoomId(String destination) {
        int lastIndex = destination.lastIndexOf('/');
        if (lastIndex != -1)
            return destination.substring(lastIndex + 1);
        else
            return "";
    }

    /**
     * 입장/퇴장시 채팅방에 안내 메시지 발송
     */
    private void sendInfoMessage(ChatMessage.MessageType messageType, String roomId, String senderName, long userCount) {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setRoomId(roomId);
        chatMessage.setType(ChatMessage.MessageType.ENTER);
        chatMessage.setSender("[알림]");
        if (ChatMessage.MessageType.ENTER.equals(messageType))
            chatMessage.setMessage(senderName + "님이 방에 입장했습니다.");
        else if (ChatMessage.MessageType.QUIT.equals(messageType))
            chatMessage.setMessage(senderName + "님이 방에서 나갔습니다.");

        chatMessage.setUserCount(userCount);
        redisTemplate.convertAndSend(channelTopic.getTopic(), chatMessage);
    }
}
