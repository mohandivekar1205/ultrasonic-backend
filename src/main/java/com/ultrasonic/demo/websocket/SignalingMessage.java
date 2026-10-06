package com.ultrasonic.demo.websocket;

import lombok.Data;
import java.util.UUID;

@Data
public class SignalingMessage {
    private String type;
    private UUID roomId;
    private UUID userId;
    private String senderId;
    private String targetId;
    private Object payload;
    private Object sdp;
    private Object candidate;
}
