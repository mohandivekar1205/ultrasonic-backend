package com.ultrasonic.demo.service;

import com.ultrasonic.demo.entity.Contact;
import com.ultrasonic.demo.entity.User;
import com.ultrasonic.demo.repository.ContactRepository;
import com.ultrasonic.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ContactService {

    @Autowired
    private ContactRepository contactRepository;

    @Autowired
    private UserRepository userRepository;

    public void addContact(UUID contactId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(username).orElseThrow();

        Contact contact = new Contact();
        contact.setUserId(currentUser.getId());
        contact.setContactId(contactId);
        contactRepository.save(contact);
    }

    public List<Contact> getContacts() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(username).orElseThrow();
        return contactRepository.findByUserId(currentUser.getId());
    }
}
