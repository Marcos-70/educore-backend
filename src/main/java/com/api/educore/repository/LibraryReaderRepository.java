package com.api.educore.repository;

import com.api.educore.model.LibraryReader;
import com.api.educore.model.Status;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LibraryReaderRepository extends JpaRepository<LibraryReader, Long> {
    List<LibraryReader> findByStatus(Status status);
    List<LibraryReader> findByNameContainingIgnoreCase(String name);
}
