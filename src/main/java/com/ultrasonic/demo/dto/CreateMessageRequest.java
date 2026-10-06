package com.ultrasonic.demo.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class CreateMessageRequest {
    private UUID id;
    private UUID senderId;
    private String type;
    private String content;
    private Long timestamp;
    private String mediaFileName;
    private String mediaMimeType;
    private Long mediaSizeBytes;
    private String mediaChecksum;
}
