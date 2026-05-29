package ru.sciencelove.repository;

import ru.sciencelove.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    // Исправлено: user.id вместо userId (обращаемся к связанной сущности)
    List<Notification> findByUser_IdAndIsReadFalse(UUID userId);

    // Исправленный @Query: n.user.id вместо n.userId
    @Query("""
        SELECT n FROM Notification n 
        WHERE n.user.id = :userId 
        ORDER BY n.createdAt DESC
        """)
    List<Notification> findByUserIdOrderByCreatedAtDesc(UUID userId);

    // Количество непрочитанных уведомлений
    long countByUser_IdAndIsReadFalse(UUID userId);

    // Все уведомления пользователя
    @Query("""
        SELECT n FROM Notification n 
        WHERE n.user.id = :userId
        ORDER BY n.createdAt DESC
        """)
    List<Notification> findAllByUserId(UUID userId);
}