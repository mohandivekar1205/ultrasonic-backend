package com.ultrasonic.demo.websocket;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.UUID;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SignalingMessage {
    private String type;
    private UUID roomId;
    private UUID userId;
    private String senderId;
    private String targetId;
    private String messageId;
    private Object payload;
    private Object sdp;
    private Object candidate;
}
