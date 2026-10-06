package com.ultrasonic.demo.service;

import com.ultrasonic.demo.dto.CreateConversationRequest;
import com.ultrasonic.demo.dto.CreateMessageRequest;
import com.ultrasonic.demo.entity.Conversation;
import com.ultrasonic.demo.entity.Message;
import com.ultrasonic.demo.entity.User;
import com.ultrasonic.demo.repository.ConversationRepository;
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

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username).orElseThrow();
    }

    public Conversation getOrCreateConversation(CreateConversationRequest request) {
        User currentUser = getCurrentUser();
        UUID userId1 = currentUser.getId();
        UUID userId2 = request.getTargetUserId();

        Optional<Conversation> existing = conversationRepository.findByUsers(userId1, userId2);
        if (existing.isPresent()) {
            return existing.get();
        }

        Conversation newConv = new Conversation();
        newConv.setUserAId(userId1);
        newConv.setUserBId(userId2);
        return conversationRepository.save(newConv);
    }

    @Autowired
    private com.ultrasonic.demo.repository.GroupRepository groupRepository;

    @Autowired
    private com.ultrasonic.demo.repository.GroupMemberRepository groupMemberRepository;

    public List<com.ultrasonic.demo.dto.ConversationSummaryDto> getConversationsForCurrentUser() {
        User currentUser = getCurrentUser();
        List<Conversation> convs = conversationRepository.findByUserAIdOrUserBId(currentUser.getId(), currentUser.getId());
        
        java.util.List<com.ultrasonic.demo.dto.ConversationSummaryDto> allSummaries = new java.util.ArrayList<>();

        // Add 1:1 Conversations
        for (Conversation c : convs) {
            com.ultrasonic.demo.dto.ConversationSummaryDto dto = new com.ultrasonic.demo.dto.ConversationSummaryDto();
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
            
            List<Message> msgs = messageRepository.findByConversationIdAndCreatedAtBefore(c.getId(), LocalDateTime.now().plusDays(1), PageRequest.of(0, 1));
            if (!msgs.isEmpty()) {
                Message last = msgs.get(0);
                dto.setLastMessage(last.getContent());
                dto.setLastMessageTime(last.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
            } else {
                dto.setLastMessage("No messages yet");
                dto.setLastMessageTime(c.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
            }
            allSummaries.add(dto);
        }

        // Add Groups
        List<com.ultrasonic.demo.entity.GroupMember> groupMembers = groupMemberRepository.findByUserId(currentUser.getId());
        for (com.ultrasonic.demo.entity.GroupMember gm : groupMembers) {
            com.ultrasonic.demo.entity.Group g = groupRepository.findById(gm.getGroupId()).orElse(null);
            if (g != null) {
                com.ultrasonic.demo.dto.ConversationSummaryDto dto = new com.ultrasonic.demo.dto.ConversationSummaryDto();
                dto.setId(g.getId());
                dto.setGroup(true);
                dto.setOtherUsername(g.getName());
                dto.setOtherDisplayName(g.getName());
                dto.setAvatarLetter(g.getName().substring(0, 1).toUpperCase());

                List<Message> msgs = messageRepository.findByGroupIdAndCreatedAtBefore(g.getId(), LocalDateTime.now().plusDays(1), PageRequest.of(0, 1));
                if (!msgs.isEmpty()) {
                    Message last = msgs.get(0);
                    dto.setLastMessage(last.getContent());
                    dto.setLastMessageTime(last.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
                } else {
                    dto.setLastMessage("Group created");
                    // Use a recent fallback time if Group doesn't have a createdAt
                    dto.setLastMessageTime(System.currentTimeMillis());
                }
                allSummaries.add(dto);
            }
        }

        allSummaries.sort((a, b) -> b.getLastMessageTime().compareTo(a.getLastMessageTime()));
        return allSummaries;
    }

    public Message createMessage(UUID conversationId, CreateMessageRequest request) {
        Message message = new Message();
        if (request.getId() != null) {
            message.setId(request.getId());
        } else {
            message.setId(UUID.randomUUID());
        }
        message.setConversationId(conversationId);
        message.setSenderId(getCurrentUser().getId());
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

    public List<Message> getMessagesBefore(UUID conversationId, Long timestamp, int limit) {
        LocalDateTime time = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
        return messageRepository.findByConversationIdAndCreatedAtBefore(conversationId, time, PageRequest.of(0, limit));
    }

    public List<Message> getMessagesAfter(UUID conversationId, Long timestamp) {
        LocalDateTime time = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
        return messageRepository.findByConversationIdAndCreatedAtAfter(conversationId, time);
    }

    public void updateMessageStatus(UUID messageId, String status) {
        Message message = messageRepository.findById(messageId).orElseThrow();
        message.setDeliveryStatus(status);
        messageRepository.save(message);
    }
}
