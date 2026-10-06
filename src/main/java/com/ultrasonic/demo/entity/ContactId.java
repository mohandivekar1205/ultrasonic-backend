package com.ultrasonic.demo.entity;

import lombok.Data;
import java.io.Serializable;
import java.util.UUID;

@Data
public class ContactId implements Serializable {
    private UUID userId;
    private UUID contactId;
}
