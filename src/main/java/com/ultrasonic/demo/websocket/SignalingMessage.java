package com.ultrasonic.demo.websocket;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SignalingMessage {
    private String type;
    private String roomId;
    private String userId;
    private String senderId;
    private String targetId;
    private String messageId;
    private Object payload;
    private Object sdp;
    private Object candidate;
}
