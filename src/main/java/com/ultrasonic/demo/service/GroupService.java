package com.ultrasonic.demo.service;

import com.ultrasonic.demo.dto.CreateMessageRequest;
import com.ultrasonic.demo.dto.GroupMemberResponseDto;
import com.ultrasonic.demo.dto.MessageResponseDto;
import com.ultrasonic.demo.entity.Group;
import com.ultrasonic.demo.entity.GroupMember;
import com.ultrasonic.demo.entity.Message;
import com.ultrasonic.demo.entity.User;
import com.ultrasonic.demo.repository.GroupMemberRepository;
import com.ultrasonic.demo.repository.GroupRepository;
import com.ultrasonic.demo.repository.MessageRepository;
import com.ultrasonic.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

@Service
public class GroupService {

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private GroupMemberRepository groupMemberRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    }

    @Transactional
    public Group createGroup(String name, List<UUID> initialMembers) {
        User currentUser = getCurrentUser();
        Group group = new Group();
        group.setName(name);
        group.setCreatedBy(currentUser.getId());
        group = groupRepository.save(group);

        // Add creator
        addMember(group.getId(), currentUser.getId());

        // Add other initial members
        if (initialMembers != null) {
            for (UUID memberId : initialMembers) {
                if (!memberId.equals(currentUser.getId())) {
                    addMember(group.getId(), memberId);
                }
            }
        }
        return group;
    }

    @Transactional
    public GroupMember addMember(UUID groupId, UUID userId) {
        // Verify group exists
        groupRepository.findById(groupId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found"));

        GroupMember gm = new GroupMember();
        gm.setGroupId(groupId);
        gm.setUserId(userId);
        return groupMemberRepository.save(gm);
    }

    public List<GroupMemberResponseDto> getMembers(UUID groupId) {
        List<GroupMember> members = groupMemberRepository.findByGroupIdWithUser(groupId);
        return members.stream().map(gm -> {
            User u = gm.getUser();
            String username = u != null ? u.getUsername() : "Unknown";
            String displayName = u != null ? u.getDisplayName() : null;
            String nameToUse = (displayName != null && !displayName.isEmpty()) ? displayName : username;
            String avatarLetter = nameToUse.substring(0, 1).toUpperCase();
            long joinedAtMs = gm.getJoinedAt() != null
                    ? gm.getJoinedAt().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                    : System.currentTimeMillis();

            return GroupMemberResponseDto.builder()
                    .userId(gm.getUserId())
                    .username(username)
                    .displayName(displayName)
                    .avatarLetter(avatarLetter)
                    .joinedAt(joinedAtMs)
                    .build();
        }).toList();
    }

    public List<MessageResponseDto> getMessages(UUID groupId, Long beforeMs, int limit) {
        LocalDateTime time = beforeMs != null ? 
            LocalDateTime.ofInstant(Instant.ofEpochMilli(beforeMs), ZoneId.systemDefault()) : 
            LocalDateTime.now().plusDays(1);
        List<Message> msgs = messageRepository.findByGroupIdAndCreatedAtBefore(groupId, time, PageRequest.of(0, limit));
        return msgs.stream().map(this::mapToResponseDto).toList();
    }

    @Transactional
    public MessageResponseDto createMessage(UUID groupId, CreateMessageRequest request) {
        User sender = getCurrentUser();
        Message message = new Message();
        if (request.getId() != null) {
            message.setId(request.getId());
        } else {
            message.setId(UUID.randomUUID());
        }

        // Enforce chk_msg_target: group message has group_id, conversation_id is null
        message.setGroupId(groupId);
        message.setConversationId(null);
        message.setSenderId(sender.getId());
        message.setContent(request.getContent());

        if (request.getType() != null) {
            message.setType(request.getType());
        }
        if (request.getTimestamp() != null) {
            message.setCreatedAt(LocalDateTime.ofInstant(Instant.ofEpochMilli(request.getTimestamp()), ZoneId.systemDefault()));
        }
        message.setMediaFileName(request.getMediaFileName());
        message.setMediaMimeType(request.getMediaMimeType());
        message.setMediaSizeBytes(request.getMediaSizeBytes());
        message.setMediaChecksum(request.getMediaChecksum());
        
        Message saved = messageRepository.save(message);
        saved.setSender(sender);
        return mapToResponseDto(saved);
    }

    private MessageResponseDto mapToResponseDto(Message m) {
        String senderUsername = m.getSender() != null ? m.getSender().getUsername() : null;
        String senderDisplayName = m.getSender() != null ? m.getSender().getDisplayName() : null;
        long createdAtMs = m.getCreatedAt() != null
                ? m.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                : System.currentTimeMillis();

        return MessageResponseDto.builder()
                .id(m.getId())
                .content(m.getContent())
                .conversationId(m.getConversationId())
                .groupId(m.getGroupId())
                .senderId(m.getSenderId())
                .senderUsername(senderUsername)
                .senderDisplayName(senderDisplayName)
                .type(m.getType())
                .deliveryStatus(m.getDeliveryStatus())
                .createdAt(createdAtMs)
                .mediaFileName(m.getMediaFileName())
                .mediaMimeType(m.getMediaMimeType())
                .mediaSizeBytes(m.getMediaSizeBytes())
                .mediaChecksum(m.getMediaChecksum())
                .build();
    }
}
