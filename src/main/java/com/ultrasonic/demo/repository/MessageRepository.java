package com.ultrasonic.demo.repository;

import com.ultrasonic.demo.entity.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface MessageRepository extends JpaRepository<Message, UUID> {

    @Query("SELECT m FROM Message m WHERE m.conversationId = :conversationId AND m.createdAt < :timestamp ORDER BY m.createdAt DESC")
    List<Message> findByConversationIdAndCreatedAtBefore(
            @Param("conversationId") UUID conversationId, 
            @Param("timestamp") LocalDateTime timestamp, 
            Pageable pageable);

    @Query("SELECT m FROM Message m WHERE m.conversationId = :conversationId AND m.createdAt > :timestamp ORDER BY m.createdAt ASC")
    List<Message> findByConversationIdAndCreatedAtAfter(
            @Param("conversationId") UUID conversationId, 
            @Param("timestamp") LocalDateTime timestamp);

    @Query("SELECT m FROM Message m WHERE m.groupId = :groupId AND m.createdAt < :timestamp ORDER BY m.createdAt DESC")
    List<Message> findByGroupIdAndCreatedAtBefore(
            @Param("groupId") UUID groupId, 
            @Param("timestamp") LocalDateTime timestamp, 
            Pageable pageable);
}
