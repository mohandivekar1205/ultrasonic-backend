package com.ultrasonic.demo.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "messages")
public class Message implements org.springframework.data.domain.Persistable<UUID> {
    @Id
    private UUID id;

    @Transient
    private boolean isNew = true;

    @Override
    public boolean isNew() {
        return isNew;
    }

    @PrePersist
    @PostLoad
    void markNotNew() {
        this.isNew = false;
    }

    @Column(name = "conversation_id", nullable = true)
    private UUID conversationId;

    @Column(name = "group_id", nullable = true)
    private UUID groupId;

    @Column(name = "sender_id", nullable = false)
    private UUID senderId;

    @Column(nullable = false, length = 20)
    private String type = "text";

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "delivery_status", length = 20)
    private String deliveryStatus = "pending";

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "media_file_name")
    private String mediaFileName;

    @Column(name = "media_mime_type", length = 100)
    private String mediaMimeType;

    @Column(name = "media_size_bytes")
    private Long mediaSizeBytes;

    @Column(name = "media_checksum", length = 64)
    private String mediaChecksum;
}
