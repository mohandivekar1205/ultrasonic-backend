package com.ultrasonic.demo.controller;

import com.ultrasonic.demo.dto.UpdateMessageStatusRequest;
import com.ultrasonic.demo.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    @Autowired
    private ChatService chatService;

    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> updateMessageStatus(@PathVariable UUID id, @RequestBody UpdateMessageStatusRequest request) {
        chatService.updateMessageStatus(id, request.getStatus());
        return ResponseEntity.ok().build();
    }
}
