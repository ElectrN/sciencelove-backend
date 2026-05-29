package ru.sciencelove.repository;

import ru.sciencelove.entity.Meeting;
import ru.sciencelove.entity.Meeting.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface MeetingRepository extends JpaRepository<Meeting, UUID> {

    List<Meeting> findByChatId(UUID chatId);

    @Query("""
        SELECT m FROM Meeting m 
        WHERE m.proposedBy.id = :userId 
        OR m.chat.student.user.id = :userId 
        OR m.chat.mentor.user.id = :userId
        ORDER BY m.startTime ASC
        """)
    List<Meeting> findByParticipantId(UUID userId);

    @Query("""
        SELECT m FROM Meeting m 
        WHERE m.status IN :statuses 
        AND m.startTime >= :now
        ORDER BY m.startTime ASC
        """)
    List<Meeting> findUpcomingByStatuses(List<Status> statuses, LocalDateTime now);
}