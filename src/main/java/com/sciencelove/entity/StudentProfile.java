package com.sciencelove.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "student_profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String university;

    private String faculty;

    @Column(nullable = false)
    private Integer course;

    @Enumerated(EnumType.STRING)
    private DegreeType degreeType;

    @ElementCollection
    private List<String> interests;

    @ElementCollection
    private List<String> goals;

    @Column(nullable = false)
    private Integer totalRequests = 0;

    @Column(nullable = false)
    private Integer totalConsultations = 0;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public enum DegreeType {
        BACHELOR, MASTER, SPECIALIST
    }
}