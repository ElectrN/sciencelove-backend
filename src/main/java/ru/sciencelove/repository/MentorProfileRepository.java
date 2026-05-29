package ru.sciencelove.repository;

import ru.sciencelove.entity.MentorProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MentorProfileRepository extends JpaRepository<MentorProfile, UUID> {

    // Поиск по ID пользователя (через связь с User)
    Optional<MentorProfile> findByUserId(UUID userId);

    // Исправлено: isActive находится в user, поэтому используем user.isActive
    List<MentorProfile> findByIsPremiumTrueAndUserIsActiveTrue();

    // Поиск менторов по интересам (с проверкой активности пользователя)
    @Query("""
        SELECT m FROM MentorProfile m 
        JOIN m.interests f 
        WHERE f.id IN :fieldIds 
        AND m.user.isActive = true
        """)
    List<MentorProfile> findByInterestIdsAndUserActive(List<UUID> fieldIds);

    // Поиск по цене и рейтингу (с проверкой активности)
    @Query("""
        SELECT m FROM MentorProfile m 
        WHERE m.hourlyRate <= :maxRate 
        AND m.rating >= :minRating
        AND m.user.isActive = true
        """)
    List<MentorProfile> findByPriceAndRatingAndUserActive(BigDecimal maxRate, BigDecimal minRating);

    // Поиск всех активных менторов
    @Query("""
        SELECT m FROM MentorProfile m 
        WHERE m.user.isActive = true 
        ORDER BY m.rating DESC, m.totalConsultations DESC
        """)
    List<MentorProfile> findAllActive();
}