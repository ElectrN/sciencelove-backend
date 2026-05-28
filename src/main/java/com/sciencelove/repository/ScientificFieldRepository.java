package com.sciencelove.repository;

import com.sciencelove.entity.ScientificField;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ScientificFieldRepository extends JpaRepository<ScientificField, UUID> {

    Optional<ScientificField> findBySlug(String slug);

    List<ScientificField> findByParentIsNullAndIsActiveTrue();

    List<ScientificField> findByParentIdAndIsActiveTrue(UUID parentId);

    @Query("SELECT sf FROM ScientificField sf WHERE sf.isActive = true AND LOWER(sf.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<ScientificField> searchByName(String keyword);

    // УПРОЩЁННЫЙ ЗАПРОС: находим только прямых потомков (без рекурсии)
    // Для полной иерархии используем сервисный слой с рекурсией на Java
    @Query("""
        SELECT sf FROM ScientificField sf 
        WHERE sf.parent.id = :parentId 
        AND sf.isActive = true
        """)
    List<ScientificField> findChildrenByParentId(UUID parentId);

    // Найти все активные поля (для кэширования)
    @Query("SELECT sf FROM ScientificField sf WHERE sf.isActive = true")
    List<ScientificField> findAllActive();
}