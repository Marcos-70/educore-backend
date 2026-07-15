package com.api.educore.repository;

import com.api.educore.model.Document;
import com.api.educore.model.DocumentModule;
import com.api.educore.model.DocumentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findByStatus(DocumentStatus status);
    List<Document> findByModule(DocumentModule module);
    List<Document> findByTitleContainingIgnoreCase(String title);
    List<Document> findBySchoolId(Long schoolId);
}
