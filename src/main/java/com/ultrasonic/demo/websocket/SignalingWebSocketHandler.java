package com.ultrasonic.demo.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
public class SignalingWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // In-memory mapping of roomId -> Set of WebSocketSessions
    private static final Map<String, Set<WebSocketSession>> ROOMS = new ConcurrentHashMap<>();
    
    // Reverse mapping to easily remove session on disconnect
    private static final Map<String, String> SESSION_ROOM_MAP = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // connection opened
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        SignalingMessage msg = objectMapper.readValue(message.getPayload(), SignalingMessage.class);
        
        String roomId = msg.getRoomId() != null ? msg.getRoomId().toString() : null;
        if (roomId == null) return;

        if ("JOIN".equals(msg.getType())) {
            Set<WebSocketSession> roomSessions = ROOMS.computeIfAbsent(roomId, k -> new CopyOnWriteArraySet<>());
            
            // Send existing peers to the new peer
            java.util.List<String> existingPeers = new java.util.ArrayList<>();
            for (WebSocketSession peer : roomSessions) {
                if (peer.isOpen()) {
                    existingPeers.add(peer.getId());
                    // Notify existing peer that a new peer joined
                    String joinMsg = "{\"type\":\"PEER_JOINED\",\"roomId\":\"" + roomId + "\",\"senderId\":\"" + session.getId() + "\"}";
                    peer.sendMessage(new TextMessage(joinMsg));
                }
            }
            
            roomSessions.add(session);
            SESSION_ROOM_MAP.put(session.getId(), roomId);
            
            String peersMsg = "{\"type\":\"ROOM_PEERS\",\"roomId\":\"" + roomId + "\",\"peers\": " + objectMapper.writeValueAsString(existingPeers) + "}";
            session.sendMessage(new TextMessage(peersMsg));
            
        } else if ("LEAVE".equals(msg.getType())) {
            Set<WebSocketSession> roomSessions = ROOMS.get(roomId);
            if (roomSessions != null) {
                roomSessions.remove(session);
                for (WebSocketSession peer : roomSessions) {
                    if (peer.isOpen()) {
                        String leaveMsg = "{\"type\":\"PEER_LEFT\",\"roomId\":\"" + roomId + "\",\"senderId\":\"" + session.getId() + "\"}";
                        peer.sendMessage(new TextMessage(leaveMsg));
                    }
                }
            }
            SESSION_ROOM_MAP.remove(session.getId());
        } else {
            // Forward OFFER, ANSWER, ICE_CANDIDATE to target peer
            msg.setSenderId(session.getId());
            String outPayload = objectMapper.writeValueAsString(msg);
            
            Set<WebSocketSession> roomSessions = ROOMS.get(roomId);
            if (roomSessions != null) {
                for (WebSocketSession peer : roomSessions) {
                    if (peer.isOpen() && peer.getId().equals(msg.getTargetId())) {
                        peer.sendMessage(new TextMessage(outPayload));
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String roomId = SESSION_ROOM_MAP.remove(session.getId());
        if (roomId != null) {
            Set<WebSocketSession> roomSessions = ROOMS.get(roomId);
            if (roomSessions != null) {
                roomSessions.remove(session);
                for (WebSocketSession peer : roomSessions) {
                    if (peer.isOpen()) {
                        String leaveMsg = "{\"type\":\"PEER_LEFT\",\"roomId\":\"" + roomId + "\",\"senderId\":\"" + session.getId() + "\"}";
                        peer.sendMessage(new TextMessage(leaveMsg));
                    }
                }
            }
        }
    }
}
