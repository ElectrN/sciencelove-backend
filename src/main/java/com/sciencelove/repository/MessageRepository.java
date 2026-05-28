package com.sciencelove.repository;

import com.sciencelove.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MessageRepository extends JpaRepository<Message, UUID> {

    List<Message> findByChatIdOrderByCreatedAtAsc(UUID chatId);

    @Query("""
        SELECT m FROM Message m 
        WHERE m.chat.id = :chatId 
        AND m.isRead = false 
        AND m.sender.id != :userId
        """)
    List<Message> findUnreadByChatAndUser(UUID chatId, UUID userId);

    long countByChatIdAndIsReadFalseAndSenderIdNot(UUID chatId, UUID userId);
}