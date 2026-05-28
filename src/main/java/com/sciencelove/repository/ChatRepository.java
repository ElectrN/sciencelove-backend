package com.sciencelove.repository;

import com.sciencelove.entity.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChatRepository extends JpaRepository<Chat, UUID> {

    @Query("""
        SELECT c FROM Chat c 
        WHERE c.student.user.id = :studentId 
        OR c.mentor.user.id = :mentorId
        ORDER BY c.lastMessageAt DESC NULLS LAST
        """)
    List<Chat> findByParticipantId(UUID studentId, UUID mentorId);

    Optional<Chat> findByStudentUserIdAndMentorUserId(UUID studentId, UUID mentorId);

    @Query("""
        SELECT c FROM Chat c 
        WHERE c.isArchived = false 
        AND (c.student.user.id = :userId OR c.mentor.user.id = :userId)
        ORDER BY c.lastMessageAt DESC NULLS LAST
        """)
    List<Chat> findActiveChatsByUserId(UUID userId);
}