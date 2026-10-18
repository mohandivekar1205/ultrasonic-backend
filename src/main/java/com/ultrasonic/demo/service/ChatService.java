package com.ultrasonic.demo.service;

import com.ultrasonic.demo.dto.ConversationSummaryDto;
import com.ultrasonic.demo.dto.CreateConversationRequest;
import com.ultrasonic.demo.dto.CreateMessageRequest;
import com.ultrasonic.demo.dto.MessageResponseDto;
import com.ultrasonic.demo.entity.Conversation;
import com.ultrasonic.demo.entity.Group;
import com.ultrasonic.demo.entity.GroupMember;
import com.ultrasonic.demo.entity.Message;
import com.ultrasonic.demo.entity.User;
import com.ultrasonic.demo.repository.ConversationRepository;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ChatService {

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private GroupMemberRepository groupMemberRepository;

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    }

    @Transactional
    public Conversation getOrCreateConversation(CreateConversationRequest request) {
        User currentUser = getCurrentUser();
        UUID userId1 = currentUser.getId();
        UUID userId2 = request.getTargetUserId();

        if (userId1.equals(userId2)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot create a conversation with yourself");
        }

        // Enforce chk_conv_order (user_a_id < user_b_id) strictly by lexical UUID order
        UUID userAId = userId1.toString().compareTo(userId2.toString()) < 0 ? userId1 : userId2;
        UUID userBId = userId1.toString().compareTo(userId2.toString()) < 0 ? userId2 : userId1;

        Optional<Conversation> existing = conversationRepository.findByUserAIdAndUserBId(userAId, userBId);
        if (existing.isPresent()) {
            return existing.get();
        }

        Conversation newConv = new Conversation();
        newConv.setUserAId(userAId);
        newConv.setUserBId(userBId);
        return conversationRepository.save(newConv);
    }

    public List<ConversationSummaryDto> getConversationsForCurrentUser() {
        User currentUser = getCurrentUser();
        List<Conversation> convs = conversationRepository.findByUserAIdOrUserBId(currentUser.getId(), currentUser.getId());
        List<ConversationSummaryDto> allSummaries = new ArrayList<>();

        // 1:1 Direct Conversations
        for (Conversation c : convs) {
            ConversationSummaryDto dto = new ConversationSummaryDto();
            dto.setId(c.getId());
            dto.setGroup(false);

            UUID otherUserId = c.getUserAId().equals(currentUser.getId()) ? c.getUserBId() : c.getUserAId();
            User otherUser = userRepository.findById(otherUserId).orElse(null);

            if (otherUser != null) {
                dto.setOtherUsername(otherUser.getUsername());
                dto.setOtherDisplayName(otherUser.getDisplayName());
                String nameToUse = (otherUser.getDisplayName() != null && !otherUser.getDisplayName().isEmpty())
                        ? otherUser.getDisplayName()
                        : otherUser.getUsername();
                dto.setAvatarLetter(nameToUse.substring(0, 1).toUpperCase());
            } else {
                dto.setOtherUsername("Unknown");
                dto.setOtherDisplayName("Unknown");
                dto.setAvatarLetter("U");
            }

            Message last = messageRepository.findFirstByConversationIdOrderByCreatedAtDesc(c.getId()).orElse(null);
            if (last != null) {
                dto.setLastMessage(last.getContent());
                long time = last.getCreatedAt() != null 
                    ? last.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                    : System.currentTimeMillis();
                dto.setLastMessageTime(time);
            } else {
                dto.setLastMessage("No messages yet");
                long time = c.getCreatedAt() != null 
                    ? c.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                    : 0L;
                dto.setLastMessageTime(time);
            }
            allSummaries.add(dto);
        }

        // Group Conversations
        List<GroupMember> groupMembers = groupMemberRepository.findByUserId(currentUser.getId());
        for (GroupMember gm : groupMembers) {
            Group g = groupRepository.findById(gm.getGroupId()).orElse(null);
            if (g != null) {
                ConversationSummaryDto dto = new ConversationSummaryDto();
                dto.setId(g.getId());
                dto.setGroup(true);
                dto.setOtherUsername(g.getName());
                dto.setOtherDisplayName(g.getName());
                dto.setAvatarLetter(g.getName().substring(0, 1).toUpperCase());

                Message last = messageRepository.findFirstByGroupIdOrderByCreatedAtDesc(g.getId()).orElse(null);
                if (last != null) {
                    dto.setLastMessage(last.getContent());
                    long time = last.getCreatedAt() != null 
                        ? last.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                        : System.currentTimeMillis();
                    dto.setLastMessageTime(time);
                } else {
                    dto.setLastMessage("Group created");
                    long createdTime = g.getCreatedAt() != null 
                            ? g.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() 
                            : 0L;
                    dto.setLastMessageTime(createdTime);
                }
                allSummaries.add(dto);
            }
        }

        allSummaries.sort((a, b) -> {
            long t1 = a.getLastMessageTime() != null ? a.getLastMessageTime() : 0L;
            long t2 = b.getLastMessageTime() != null ? b.getLastMessageTime() : 0L;
            return Long.compare(t2, t1);
        });
        return allSummaries;
    }

    @Transactional
    public MessageResponseDto createMessage(UUID conversationId, CreateMessageRequest request) {
        User sender = getCurrentUser();
        Message message = new Message();
        if (request.getId() != null) {
            message.setId(request.getId());
        } else {
            message.setId(UUID.randomUUID());
        }

        // Enforce chk_msg_target: 1:1 conversation has conversation_id, group_id is null
        message.setConversationId(conversationId);
        message.setGroupId(null);
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

    public List<MessageResponseDto> getMessagesBefore(UUID conversationId, Long timestamp, int limit) {
        LocalDateTime time = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
        List<Message> msgs = messageRepository.findByConversationIdAndCreatedAtBefore(conversationId, time, PageRequest.of(0, limit));
        return msgs.stream().map(this::mapToResponseDto).toList();
    }

    public List<MessageResponseDto> getMessagesAfter(UUID conversationId, Long timestamp) {
        LocalDateTime time = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
        List<Message> msgs = messageRepository.findByConversationIdAndCreatedAtAfter(conversationId, time);
        return msgs.stream().map(this::mapToResponseDto).toList();
    }

    @Transactional
    public void updateMessageStatus(UUID messageId, String status) {
        messageRepository.findById(messageId).ifPresent(message -> {
            message.setDeliveryStatus(status);
            messageRepository.save(message);
        });
    }

    public MessageResponseDto mapToResponseDto(Message m) {
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
