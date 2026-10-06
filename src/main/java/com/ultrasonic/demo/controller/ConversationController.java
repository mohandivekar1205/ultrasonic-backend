package com.ultrasonic.demo.controller;

import com.ultrasonic.demo.dto.CreateConversationRequest;
import com.ultrasonic.demo.dto.CreateMessageRequest;
import com.ultrasonic.demo.entity.Conversation;
import com.ultrasonic.demo.entity.Message;
import com.ultrasonic.demo.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/conversations")
public class ConversationController {

    @Autowired
    private ChatService chatService;

    @PostMapping
    public ResponseEntity<Conversation> getOrCreateConversation(@RequestBody CreateConversationRequest request) {
        Conversation conversation = chatService.getOrCreateConversation(request);
        return ResponseEntity.ok(conversation);
    }

    @GetMapping
    public ResponseEntity<List<com.ultrasonic.demo.dto.ConversationSummaryDto>> getConversations() {
        return ResponseEntity.ok(chatService.getConversationsForCurrentUser());
    }

    @GetMapping("/{id}/messages")
    public ResponseEntity<List<Message>> getMessages(
            @PathVariable UUID id,
            @RequestParam(required = false) Long before,
            @RequestParam(required = false) Long after,
            @RequestParam(defaultValue = "50") int limit) {
        
        List<Message> messages;
        if (after != null) {
            messages = chatService.getMessagesAfter(id, after);
        } else {
            if (before == null) {
                before = Instant.now().toEpochMilli();
            }
            messages = chatService.getMessagesBefore(id, before, limit);
        }
        return ResponseEntity.ok(messages);
    }

    @PostMapping("/{id}/messages")
    public ResponseEntity<Message> createMessage(@PathVariable UUID id, @RequestBody CreateMessageRequest request) {
        Message message = chatService.createMessage(id, request);
        return ResponseEntity.ok(message);
    }
}
