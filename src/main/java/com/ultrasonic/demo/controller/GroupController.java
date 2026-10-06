package com.ultrasonic.demo.controller;

import com.ultrasonic.demo.entity.Group;
import com.ultrasonic.demo.entity.GroupMember;
import com.ultrasonic.demo.entity.Message;
import com.ultrasonic.demo.service.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/groups")
public class GroupController {

    @Autowired
    private GroupService groupService;

    @PostMapping
    public ResponseEntity<Group> createGroup(@RequestBody CreateGroupRequest request) {
        return ResponseEntity.ok(groupService.createGroup(request.getName(), request.getMemberIds()));
    }

    @PostMapping("/{id}/members")
    public ResponseEntity<GroupMember> addMember(@PathVariable UUID id, @RequestBody AddMemberRequest request) {
        return ResponseEntity.ok(groupService.addMember(id, request.getUserId()));
    }

    @GetMapping("/{id}/members")
    public ResponseEntity<List<GroupMember>> getMembers(@PathVariable UUID id) {
        return ResponseEntity.ok(groupService.getMembers(id));
    }

    @GetMapping("/{id}/messages")
    public ResponseEntity<List<Message>> getGroupMessages(
            @PathVariable UUID id,
            @RequestParam(required = false) Long before,
            @RequestParam(defaultValue = "50") int limit) {
        return ResponseEntity.ok(groupService.getMessages(id, before, limit));
    }

    @PostMapping("/{id}/messages")
    public ResponseEntity<Message> createGroupMessage(
            @PathVariable UUID id,
            @RequestBody com.ultrasonic.demo.dto.CreateMessageRequest request) {
        return ResponseEntity.ok(groupService.createMessage(id, request));
    }
}

class CreateGroupRequest {
    private String name;
    private List<UUID> memberIds;
    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public List<UUID> getMemberIds() { return memberIds; }
    public void setMemberIds(List<UUID> memberIds) { this.memberIds = memberIds; }
}

class AddMemberRequest {
    private UUID userId;
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
}
