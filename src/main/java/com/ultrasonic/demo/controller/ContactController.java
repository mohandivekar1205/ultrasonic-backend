package com.ultrasonic.demo.controller;

import com.ultrasonic.demo.entity.Contact;
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
    public ResponseEntity<?> addContact(@PathVariable UUID contactId) {
        contactService.addContact(contactId);
        return ResponseEntity.ok("Contact added");
    }

    @GetMapping
    public List<Contact> getContacts() {
        return contactService.getContacts();
    }
}
