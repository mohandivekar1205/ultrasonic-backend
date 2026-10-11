package com.ultrasonic.demo.controller;

import com.ultrasonic.demo.dto.ContactResponseDto;
import com.ultrasonic.demo.service.ContactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/contacts")
public class ContactController {

    @Autowired
    private ContactService contactService;

    @PostMapping("/{contactId}")
    public ResponseEntity<Void> addContact(@PathVariable UUID contactId) {
        contactService.addContact(contactId);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<ContactResponseDto>> getContacts() {
        return ResponseEntity.ok(contactService.getContacts());
    }

    @DeleteMapping("/{contactId}")
    public ResponseEntity<Void> deleteContact(@PathVariable UUID contactId) {
        contactService.deleteContact(contactId);
        return ResponseEntity.noContent().build();
    }
}
