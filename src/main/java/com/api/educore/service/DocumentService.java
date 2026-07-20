package com.api.educore.service;

import com.api.educore.dto.DocumentDTO;
import com.api.educore.model.Document;
import com.api.educore.model.DocumentStatus;
import com.api.educore.model.School;
import com.api.educore.model.User;
import com.api.educore.repository.DocumentRepository;
import com.api.educore.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;

    private School getCurrentSchool() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElse(null);
        return user != null ? user.getSchool() : null;
    }

    public List<DocumentDTO> findAll() {
        School school = getCurrentSchool();
        if (school == null) return List.of();
        return documentRepository.findBySchoolId(school.getId()).stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<DocumentDTO> findByStatus(DocumentStatus status) {
        School school = getCurrentSchool();
        if (school == null) return List.of();
        return documentRepository.findBySchoolId(school.getId()).stream()
                .filter(d -> d.getStatus() == status)
                .map(this::toDTO).collect(Collectors.toList());
    }

    public DocumentDTO create(DocumentDTO dto) {
        Document doc = new Document();
        doc.setSchool(getCurrentSchool());
        mapDocument(dto, doc);
        return toDTO(documentRepository.save(doc));
    }

    public DocumentDTO update(Long id, DocumentDTO dto) {
        Document existing = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Documento não encontrado"));
        mapDocument(dto, existing);
        return toDTO(documentRepository.save(existing));
    }

    public DocumentDTO updateStatus(Long id, DocumentStatus status) {
        Document doc = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Documento não encontrado"));
        doc.setStatus(status);
        return toDTO(documentRepository.save(doc));
    }

    public void delete(Long id) {
        documentRepository.deleteById(id);
    }

    private void mapDocument(DocumentDTO dto, Document d) {
        d.setTitle(dto.getTitle());
        d.setSubject(dto.getSubject());
        d.setReference(dto.getReference());
        d.setModule(dto.getModule());
        d.setStatus(dto.getStatus() != null ? dto.getStatus() : DocumentStatus.DRAFT);
        d.setCategory(dto.getCategory());
        d.setDocumentType(dto.getDocumentType());
        d.setFormat(dto.getFormat());
        d.setFilePath(dto.getFilePath());
        d.setDepartment(dto.getDepartment());
        d.setDescription(dto.getDescription());
        d.setUploadedBy(dto.getUploadedBy());
    }

    private DocumentDTO toDTO(Document d) {
        DocumentDTO dto = new DocumentDTO();
        dto.setId(d.getId());
        dto.setTitle(d.getTitle());
        dto.setSubject(d.getSubject());
        dto.setReference(d.getReference());
        dto.setModule(d.getModule());
        dto.setStatus(d.getStatus());
        dto.setCategory(d.getCategory());
        dto.setDocumentType(d.getDocumentType());
        dto.setFormat(d.getFormat());
        dto.setFilePath(d.getFilePath());
        dto.setDepartment(d.getDepartment());
        dto.setDescription(d.getDescription());
        dto.setUploadedBy(d.getUploadedBy());
        dto.setUserId(d.getUser() != null ? d.getUser().getId() : null);
        return dto;
    }
}
