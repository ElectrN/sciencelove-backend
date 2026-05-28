package com.sciencelove.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "mentor_profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MentorProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String degree;

    private String university;

    private String department;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @ElementCollection
    private List<String> expertise;

    @Column(precision = 3, scale = 2)
    private BigDecimal rating = BigDecimal.ZERO;

    @Column(nullable = false)
    private Integer totalConsultations = 0;

    @Column(nullable = false)
    private Integer totalReviews = 0;

    @Column(precision = 10, scale = 2)
    private BigDecimal hourlyRate;

    @Column(nullable = false)
    private Integer minSessionMinutes = 30;

    @ElementCollection
    private List<Integer> availableDays;

    private LocalTime availableHoursStart;

    private LocalTime availableHoursEnd;

    @Column(nullable = false)
    private Boolean isPremium = false;

    private LocalDateTime premiumExpiresAt;

    @ManyToMany
    @JoinTable(
            name = "mentor_interests",
            joinColumns = @JoinColumn(name = "mentor_id"),
            inverseJoinColumns = @JoinColumn(name = "field_id")
    )
    private List<ScientificField> interests;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}