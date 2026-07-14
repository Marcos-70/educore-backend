package com.api.educore.dto;

import com.api.educore.model.DocumentModule;
import com.api.educore.model.DocumentStatus;
import lombok.Data;

@Data
public class DocumentDTO {
    private Long id;
    private String title;
    private String subject;
    private String reference;
    private DocumentModule module;
    private DocumentStatus status;
    private String category;
    private String documentType;
    private String format;
    private String filePath;
    private String department;
    private String description;
    private String uploadedBy;
    private Long userId;
}
