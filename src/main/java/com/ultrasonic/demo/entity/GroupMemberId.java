package com.ultrasonic.demo.entity;

import lombok.Data;
import java.io.Serializable;
import java.util.UUID;

@Data
public class GroupMemberId implements Serializable {
    private UUID groupId;
    private UUID userId;
}
