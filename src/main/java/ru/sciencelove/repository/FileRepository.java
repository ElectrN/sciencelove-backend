package ru.sciencelove.repository;

import ru.sciencelove.entity.File;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FileRepository extends JpaRepository<File, UUID> {

    List<File> findByUploadedBy(UUID userId);

    List<File> findByContextTypeAndContextId(String contextType, UUID contextId);
}