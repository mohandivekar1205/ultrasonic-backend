package com.ultrasonic.demo.service;

import com.ultrasonic.demo.entity.Group;
import com.ultrasonic.demo.entity.GroupMember;
import com.ultrasonic.demo.entity.Message;
import com.ultrasonic.demo.repository.GroupMemberRepository;
import com.ultrasonic.demo.repository.GroupRepository;
import com.ultrasonic.demo.repository.MessageRepository;
import com.ultrasonic.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

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

    private UUID getCurrentUserId() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username).orElseThrow().getId();
    }

    public Group createGroup(String name, List<UUID> initialMembers) {
        Group group = new Group();
        group.setName(name);
        group.setCreatedBy(getCurrentUserId());
        group = groupRepository.save(group);

        // Add creator
        addMember(group.getId(), getCurrentUserId());

        // Add others
        if (initialMembers != null) {
            for (UUID memberId : initialMembers) {
                if (!memberId.equals(getCurrentUserId())) {
                    addMember(group.getId(), memberId);
                }
            }
        }
        return group;
    }

    public GroupMember addMember(UUID groupId, UUID userId) {
        GroupMember gm = new GroupMember();
        gm.setGroupId(groupId);
        gm.setUserId(userId);
        return groupMemberRepository.save(gm);
    }

    public List<GroupMember> getMembers(UUID groupId) {
        return groupMemberRepository.findByGroupId(groupId);
    }

    public List<Message> getMessages(UUID groupId, Long beforeMs, int limit) {
        LocalDateTime time = beforeMs != null ? 
            LocalDateTime.ofInstant(Instant.ofEpochMilli(beforeMs), ZoneId.systemDefault()) : 
            LocalDateTime.now().plusDays(1);
        return messageRepository.findByGroupIdAndCreatedAtBefore(groupId, time, PageRequest.of(0, limit));
    }

    public Message createMessage(UUID groupId, com.ultrasonic.demo.dto.CreateMessageRequest request) {
        Message message = new Message();
        if (request.getId() != null) {
            message.setId(request.getId());
        } else {
            message.setId(UUID.randomUUID());
        }
        message.setGroupId(groupId);
        message.setSenderId(getCurrentUserId());
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
        
        return messageRepository.save(message);
    }
}
