package com.ultrasonic.demo.repository;

import com.ultrasonic.demo.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConversationRepository extends JpaRepository<Conversation, UUID> {
    
    @Query("SELECT c FROM Conversation c WHERE (c.userAId = :userId1 AND c.userBId = :userId2) OR (c.userAId = :userId2 AND c.userBId = :userId1)")
    Optional<Conversation> findByUsers(@Param("userId1") UUID userId1, @Param("userId2") UUID userId2);

    List<Conversation> findByUserAIdOrUserBId(UUID userAId, UUID userBId);
}
