package com.websocket.chat.controller;

import com.websocket.chat.model.ChatMessage;
import com.websocket.chat.model.ChatRoom;
import com.websocket.chat.service.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import javax.annotation.PostConstruct;

@Slf4j
@RequiredArgsConstructor
@Controller
public class ChatController {

    private final RedisTemplate redisTemplate;
    private final JwtTokenProvider jwtTokenProvider;
    private final ChannelTopic channelTopic;

    private ValueOperations valueOps;

    @PostConstruct
    private void init() {
        valueOps = redisTemplate.opsForValue();
    }

    /**
     * websocket "/pub/chat/message"로 들어오는 메시징을 처리한다.
     */
    @MessageMapping("/chat/message")
    public void message(ChatMessage message, @Header("token") String token) {
        String nickname = jwtTokenProvider.getUserNameFromJwt(token);
        // 로그인 회원 정보로 대화명 설정
        message.setSender(nickname);
        // 채팅방 입장/퇴장시에는 대화명과 메시지를 자동으로 세팅한다.
//        if (ChatMessage.MessageType.ENTER.equals(message.getType())) {
//            message.setSender("[알림]");
//            message.setMessage(nickname + "님이 방에 입장했습니다.");
//        }
        // 채팅방 인원수 조회
        Long userCnt = Long.valueOf((String) valueOps.get(ChatRoom.USER_COUNT + message.getRoomId()));
        message.setUserCount(userCnt);
        // Websocket에 발행된 메시지를 redis로 발행(publish)
        redisTemplate.convertAndSend(channelTopic.getTopic(), message);
    }
}
