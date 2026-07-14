package com.api.educore.dto;

import com.api.educore.model.BookStatus;
import lombok.Data;

@Data
public class LibraryBookDTO {
    private Long id;
    private String code;
    private String isbn;
    private String title;
    private String author;
    private String category;
    private String publisher;
    private int year;
    private int totalCopies;
    private int availableCopies;
    private String location;
    private BookStatus status;
}
