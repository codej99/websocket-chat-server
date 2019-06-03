package com.websocket.chat.repo;

import com.websocket.chat.model.ChatRoom;
import com.websocket.chat.pubsub.RedisSubscriber;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Repository
public class ChatRoomRepository {
    // topic에 발행되는 액션을 처리할 Listner
    private final RedisMessageListenerContainer redisMessageListener;
    // 구독자
    private final RedisSubscriber redisSubscriber;
    // Redis
    private final RedisTemplate<String, Object> redisTemplate;
    private HashOperations<String, String, ChatRoom> opsHashChatRoom;
    private final String CHAT_ROOMS = "CHAT_ROOM";
    // 서버별로 필요시 생성된다.
    private Map<String, ChannelTopic> topics;

    @PostConstruct
    private void init() {
        opsHashChatRoom = redisTemplate.opsForHash();
        topics = new HashMap<>();
    }

    public List<ChatRoom> findAllRoom() {
        return opsHashChatRoom.values(CHAT_ROOMS);
    }

    public ChatRoom findRoomById(String id) {
        return opsHashChatRoom.get(CHAT_ROOMS, id);
    }

    public ChatRoom createChatRoom(String name) {
        ChatRoom chatRoom = ChatRoom.create(name);
        opsHashChatRoom.put(CHAT_ROOMS, chatRoom.getRoomId(), chatRoom);

        ChannelTopic channel = new ChannelTopic(chatRoom.getRoomId());
        redisMessageListener.addMessageListener(redisSubscriber, channel);
        topics.put(chatRoom.getRoomId(), channel);
        return chatRoom;
    }

    public ChannelTopic getTopic(String roomId) {
        return topics.get(roomId);
    }
}
