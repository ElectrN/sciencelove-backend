package ru.sciencelove.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "scientific_fields")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScientificField {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private ScientificField parent;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String slug;

    private String description;

    @Column(nullable = false)
    private Integer level = 0;

    @ElementCollection
    private List<String> synonyms;

    @ElementCollection
    private List<UUID> relatedFields;

    @Column(nullable = false)
    private Boolean isActive = true;

    @Column(nullable = false)
    private Integer totalMentors = 0;

    @Column(nullable = false)
    private Integer totalRequests = 0;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    private List<ScientificField> children;
}