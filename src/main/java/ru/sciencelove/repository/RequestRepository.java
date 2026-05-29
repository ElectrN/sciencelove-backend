package ru.sciencelove.repository;

import ru.sciencelove.entity.Request;
import ru.sciencelove.entity.Request.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface RequestRepository extends JpaRepository<Request, UUID> {

    List<Request> findByStudentUserId(UUID studentUserId);

    List<Request> findByStatus(Status status);

    @Query("""
        SELECT r FROM Request r 
        WHERE r.status = :status 
        AND r.deadline >= :today
        ORDER BY r.createdAt DESC
        """)
    List<Request> findActiveByDeadline(Status status, LocalDate today);

    @Query("""
        SELECT r FROM Request r 
        JOIN r.fields f 
        WHERE f.id IN :fieldIds 
        AND r.status = 'OPEN'
        """)
    List<Request> findByFieldIdsAndOpen(List<UUID> fieldIds);
}