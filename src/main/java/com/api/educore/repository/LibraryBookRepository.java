package com.api.educore.repository;

import com.api.educore.model.BookStatus;
import com.api.educore.model.LibraryBook;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LibraryBookRepository extends JpaRepository<LibraryBook, Long> {
    List<LibraryBook> findByStatus(BookStatus status);
    List<LibraryBook> findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(String title, String author);
    List<LibraryBook> findByCategory(String category);
    List<LibraryBook> findBySchoolId(Long schoolId);
}
