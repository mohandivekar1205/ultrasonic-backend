package com.ultrasonic.demo.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "contacts")
@IdClass(ContactId.class)
public class Contact {
    @Id
    @Column(name = "user_id")
    private UUID userId;

    @Id
    @Column(name = "contact_id")
    private UUID contactId;

    private LocalDateTime addedAt = LocalDateTime.now();
}
