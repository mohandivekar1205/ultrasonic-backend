package com.ultrasonic.demo.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class CreateConversationRequest {
    private UUID targetUserId;
}
