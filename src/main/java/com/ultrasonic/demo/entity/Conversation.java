package com.ultrasonic.demo.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "conversations", uniqueConstraints = {
    @UniqueConstraint(name = "uq_conversations_users", columnNames = {"user_a_id", "user_b_id"})
})
public class Conversation {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_a_id", nullable = false)
    private UUID userAId;

    @Column(name = "user_b_id", nullable = false)
    private UUID userBId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_a_id", insertable = false, updatable = false)
    private User userA;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_b_id", insertable = false, updatable = false)
    private User userB;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
