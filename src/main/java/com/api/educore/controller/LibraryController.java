package com.api.educore.controller;

import com.api.educore.dto.LibraryBookDTO;
import com.api.educore.dto.LibraryLoanDTO;
import com.api.educore.dto.LibraryReaderDTO;
import com.api.educore.service.LibraryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/library")
@RequiredArgsConstructor
public class LibraryController {

    private final LibraryService libraryService;

    // Books
    @GetMapping("/books")
    public ResponseEntity<List<LibraryBookDTO>> findAllBooks() {
        return ResponseEntity.ok(libraryService.findAllBooks());
    }

    @PostMapping("/books")
    public ResponseEntity<LibraryBookDTO> createBook(@RequestBody LibraryBookDTO dto) {
        return ResponseEntity.ok(libraryService.createBook(dto));
    }

    @PutMapping("/books/{id}")
    public ResponseEntity<LibraryBookDTO> updateBook(@PathVariable Long id, @RequestBody LibraryBookDTO dto) {
        return ResponseEntity.ok(libraryService.updateBook(id, dto));
    }

    @DeleteMapping("/books/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        libraryService.deleteBook(id);
        return ResponseEntity.noContent().build();
    }

    // Loans
    @GetMapping("/loans")
    public ResponseEntity<List<LibraryLoanDTO>> findAllLoans() {
        return ResponseEntity.ok(libraryService.findAllLoans());
    }

    @GetMapping("/loans/active")
    public ResponseEntity<List<LibraryLoanDTO>> findActiveLoans() {
        return ResponseEntity.ok(libraryService.findActiveLoans());
    }

    @PostMapping("/loans")
    public ResponseEntity<LibraryLoanDTO> createLoan(@RequestBody LibraryLoanDTO dto) {
        return ResponseEntity.ok(libraryService.createLoan(dto));
    }

    @PostMapping("/loans/{id}/return")
    public ResponseEntity<LibraryLoanDTO> returnBook(@PathVariable Long id) {
        return ResponseEntity.ok(libraryService.returnBook(id));
    }

    // Readers
    @GetMapping("/readers")
    public ResponseEntity<List<LibraryReaderDTO>> findAllReaders() {
        return ResponseEntity.ok(libraryService.findAllReaders());
    }

    @PostMapping("/readers")
    public ResponseEntity<LibraryReaderDTO> createReader(@RequestBody LibraryReaderDTO dto) {
        return ResponseEntity.ok(libraryService.createReader(dto));
    }

    @PutMapping("/readers/{id}")
    public ResponseEntity<LibraryReaderDTO> updateReader(@PathVariable Long id, @RequestBody LibraryReaderDTO dto) {
        return ResponseEntity.ok(libraryService.updateReader(id, dto));
    }

    @DeleteMapping("/readers/{id}")
    public ResponseEntity<Void> deleteReader(@PathVariable Long id) {
        libraryService.deleteReader(id);
        return ResponseEntity.noContent().build();
    }
}
