package com.ultrasonic.demo.repository;

import com.ultrasonic.demo.entity.Contact;
import com.ultrasonic.demo.entity.ContactId;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface ContactRepository extends JpaRepository<Contact, ContactId> {
    List<Contact> findByUserId(UUID userId);
}
