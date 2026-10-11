package com.ultrasonic.demo.service;

import com.ultrasonic.demo.dto.ContactResponseDto;
import com.ultrasonic.demo.entity.Contact;
import com.ultrasonic.demo.entity.User;
import com.ultrasonic.demo.repository.ContactRepository;
import com.ultrasonic.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

@Service
public class ContactService {

    @Autowired
    private ContactRepository contactRepository;

    @Autowired
    private UserRepository userRepository;

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    }

    @Transactional
    public void addContact(UUID contactId) {
        User currentUser = getCurrentUser();

        // Enforce chk_contacts_not_self (contact_id <> user_id)
        if (currentUser.getId().equals(contactId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot add yourself as a contact");
        }

        User targetUser = userRepository.findById(contactId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Contact user not found"));

        if (contactRepository.findByUserIdAndContactId(currentUser.getId(), contactId).isPresent()) {
            return; // Already added
        }

        Contact contact = new Contact();
        contact.setUserId(currentUser.getId());
        contact.setContactId(targetUser.getId());
        contactRepository.save(contact);
    }

    public List<ContactResponseDto> getContacts() {
        User currentUser = getCurrentUser();
        List<Contact> contacts = contactRepository.findByUserIdWithContactUser(currentUser.getId());

        return contacts.stream().map(c -> {
            User cu = c.getContactUser();
            String username = cu != null ? cu.getUsername() : "Unknown";
            String displayName = cu != null ? cu.getDisplayName() : null;
            String nameToUse = (displayName != null && !displayName.isEmpty()) ? displayName : username;
            String avatarLetter = nameToUse.substring(0, 1).toUpperCase();
            long addedAtMs = c.getAddedAt() != null 
                    ? c.getAddedAt().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                    : System.currentTimeMillis();

            return ContactResponseDto.builder()
                    .contactId(c.getContactId())
                    .username(username)
                    .displayName(displayName)
                    .avatarLetter(avatarLetter)
                    .addedAt(addedAtMs)
                    .build();
        }).toList();
    }

    @Transactional
    public void deleteContact(UUID contactId) {
        User currentUser = getCurrentUser();
        contactRepository.deleteByUserIdAndContactId(currentUser.getId(), contactId);
    }
}
