package com.websocket.chat.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
public class ChatRoom implements Serializable {

    private static final long serialVersionUID = 6494678977089006639L;
    // 채팅룸에 입장한 클라이언트수를 저장하는 Redis Cache Key
    public static final String USER_COUNT = "USER_COUNT";
    // 채팅룸에 입장한 클라이언트의 sessionId와 채팅룸 id를 맵핑한 정보를 담는 Redis Cache Key
    public static final String USER_ENTRY = "USER_ENTRY";

    private String roomId;
    private String name;
    private long userCount; // 채팅방 인원수

    public static ChatRoom create(String name) {
        ChatRoom chatRoom = new ChatRoom();
        chatRoom.roomId = UUID.randomUUID().toString();
        chatRoom.name = name;
        return chatRoom;
    }
}
