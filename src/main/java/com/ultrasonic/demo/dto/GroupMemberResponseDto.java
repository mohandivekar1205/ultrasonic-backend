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
public class GroupMemberResponseDto {
    private UUID userId;
    private String username;
    private String displayName;
    private String avatarLetter;
    private Long joinedAt;
}
