package com.ultrasonic.demo.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class ConversationSummaryDto {
    private UUID id;
    private String otherUsername;
    private String otherDisplayName;
    private String lastMessage;
    private Long lastMessageTime;
    private String avatarLetter;
    private boolean isGroup;
}
