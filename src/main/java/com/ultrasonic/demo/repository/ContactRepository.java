package com.ultrasonic.demo.repository;

import com.ultrasonic.demo.entity.Contact;
import com.ultrasonic.demo.entity.ContactId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ContactRepository extends JpaRepository<Contact, ContactId> {
    
    @Query("SELECT c FROM Contact c LEFT JOIN FETCH c.contactUser WHERE c.userId = :userId ORDER BY c.addedAt DESC")
    List<Contact> findByUserIdWithContactUser(@Param("userId") UUID userId);

    List<Contact> findByUserId(UUID userId);

    Optional<Contact> findByUserIdAndContactId(UUID userId, UUID contactId);

    void deleteByUserIdAndContactId(UUID userId, UUID contactId);
}
