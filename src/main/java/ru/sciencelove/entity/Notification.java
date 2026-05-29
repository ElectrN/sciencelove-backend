package ru.sciencelove.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String type;  // 'NEW_MESSAGE', 'MEETING_PROPOSAL', 'REMINDER', etc.

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    @Column(name = "target_type")
    private String targetType;  // 'chat', 'meeting', 'request'

    @Column(name = "target_id")
    private UUID targetId;

    @Column(nullable = false)
    private Boolean isRead = false;

    private LocalDateTime readAt;

    @Column(columnDefinition = "jsonb")
    private String actions;  // JSON: [{"label": "Ответить", "action": "REPLY"}]

    @CreationTimestamp
    private LocalDateTime createdAt;
}