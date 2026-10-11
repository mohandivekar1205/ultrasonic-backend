package com.ultrasonic.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponseDto {
    private UUID id;
    private String content;
    private UUID conversationId;
    private UUID groupId;
    private UUID senderId;
    private String senderUsername;
    private String senderDisplayName;
    private String type;
    private String deliveryStatus;
    private Long createdAt;
    private String mediaFileName;
    private String mediaMimeType;
    private Long mediaSizeBytes;
    private String mediaChecksum;
}
