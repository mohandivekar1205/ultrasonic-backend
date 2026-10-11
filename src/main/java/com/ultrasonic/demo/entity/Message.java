package com.ultrasonic.demo.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.domain.Persistable;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "messages")
public class Message implements Persistable<UUID> {
    @Id
    private UUID id;

    @Transient
    private boolean isNew = true;

    @Override
    public boolean isNew() {
        return isNew;
    }

    @Column(name = "conversation_id")
    private UUID conversationId;

    @Column(name = "group_id")
    private UUID groupId;

    @Column(name = "sender_id", nullable = false)
    private UUID senderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", insertable = false, updatable = false)
    private Conversation conversation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", insertable = false, updatable = false)
    private Group group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", insertable = false, updatable = false)
    private User sender;

    @Column(nullable = false, length = 20)
    private String type = "text";

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "delivery_status", length = 20)
    private String deliveryStatus = "pending";

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "media_file_name", length = 255)
    private String mediaFileName;

    @Column(name = "media_mime_type", length = 100)
    private String mediaMimeType;

    @Column(name = "media_size_bytes")
    private Long mediaSizeBytes;

    @Column(name = "media_checksum", length = 64)
    private String mediaChecksum;

    @PrePersist
    void prePersist() {
        this.isNew = false;
        validateTarget();
    }

    @PreUpdate
    void preUpdate() {
        validateTarget();
    }

    @PostLoad
    void markNotNew() {
        this.isNew = false;
    }

    private void validateTarget() {
        boolean valid = (conversationId != null && groupId == null) || (conversationId == null && groupId != null);
        if (!valid) {
            throw new IllegalStateException("Violation of chk_msg_target: Message must belong to either a conversation OR a group.");
        }
    }
}
