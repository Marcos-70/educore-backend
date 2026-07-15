package com.api.educore.service;

import com.api.educore.dto.LibraryBookDTO;
import com.api.educore.dto.LibraryLoanDTO;
import com.api.educore.dto.LibraryReaderDTO;
import com.api.educore.model.*;
import com.api.educore.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LibraryService {

    private final LibraryBookRepository bookRepository;
    private final LibraryLoanRepository loanRepository;
    private final LibraryReaderRepository readerRepository;
    private final UserRepository userRepository;

    private School getCurrentSchool() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElse(null);
        return user != null ? user.getSchool() : null;
    }

    // Books
    public List<LibraryBookDTO> findAllBooks() {
        School school = getCurrentSchool();
        if (school == null) return List.of();
        return bookRepository.findBySchoolId(school.getId()).stream().map(this::toBookDTO).collect(Collectors.toList());
    }

    public LibraryBookDTO createBook(LibraryBookDTO dto) {
        LibraryBook book = new LibraryBook();
        book.setSchool(getCurrentSchool());
        mapBook(dto, book);
        return toBookDTO(bookRepository.save(book));
    }

    public LibraryBookDTO updateBook(Long id, LibraryBookDTO dto) {
        LibraryBook existing = bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Livro não encontrado"));
        mapBook(dto, existing);
        return toBookDTO(bookRepository.save(existing));
    }

    public void deleteBook(Long id) {
        bookRepository.deleteById(id);
    }

    // Loans
    public List<LibraryLoanDTO> findAllLoans() {
        School school = getCurrentSchool();
        if (school == null) return List.of();
        return loanRepository.findBySchoolId(school.getId()).stream().map(this::toLoanDTO).collect(Collectors.toList());
    }

    public List<LibraryLoanDTO> findActiveLoans() {
        return loanRepository.findByStatus(LoanStatus.ACTIVE)
                .stream().map(this::toLoanDTO).collect(Collectors.toList());
    }

    public LibraryLoanDTO createLoan(LibraryLoanDTO dto) {
        LibraryBook book = bookRepository.findById(dto.getBookId())
                .orElseThrow(() -> new RuntimeException("Livro não encontrado"));
        LibraryReader reader = readerRepository.findById(dto.getReaderId())
                .orElseThrow(() -> new RuntimeException("Leitor não encontrado"));

        if (book.getAvailableCopies() <= 0) {
            throw new RuntimeException("Não há cópias disponíveis");
        }

        LibraryLoan loan = new LibraryLoan();
        loan.setBook(book);
        loan.setReader(reader);
        loan.setSchool(getCurrentSchool());
        loan.setLoanDate(LocalDate.now());
        loan.setDueDate(LocalDate.now().plusDays(14));
        loan.setStatus(LoanStatus.ACTIVE);
        loan.setNotes(dto.getNotes());

        book.setAvailableCopies(book.getAvailableCopies() - 1);
        bookRepository.save(book);

        return toLoanDTO(loanRepository.save(loan));
    }

    public LibraryLoanDTO returnBook(Long loanId) {
        LibraryLoan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Empréstimo não encontrado"));
        loan.setReturnDate(LocalDate.now());
        loan.setStatus(LoanStatus.RETURNED);

        LibraryBook book = loan.getBook();
        book.setAvailableCopies(book.getAvailableCopies() + 1);
        bookRepository.save(book);

        return toLoanDTO(loanRepository.save(loan));
    }

    // Readers
    public List<LibraryReaderDTO> findAllReaders() {
        School school = getCurrentSchool();
        if (school == null) return List.of();
        return readerRepository.findBySchoolId(school.getId()).stream().map(this::toReaderDTO).collect(Collectors.toList());
    }

    public LibraryReaderDTO createReader(LibraryReaderDTO dto) {
        LibraryReader reader = new LibraryReader();
        reader.setSchool(getCurrentSchool());
        mapReader(dto, reader);
        return toReaderDTO(readerRepository.save(reader));
    }

    public LibraryReaderDTO updateReader(Long id, LibraryReaderDTO dto) {
        LibraryReader existing = readerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Leitor não encontrado"));
        mapReader(dto, existing);
        return toReaderDTO(readerRepository.save(existing));
    }

    public void deleteReader(Long id) {
        long activeLoans = loanRepository.countByReaderIdAndStatus(id, LoanStatus.ACTIVE);
        if (activeLoans > 0) {
            throw new RuntimeException("Leitor possui empréstimos ativos");
        }
        readerRepository.deleteById(id);
    }

    // Mapping
    private void mapBook(LibraryBookDTO dto, LibraryBook b) {
        b.setCode(dto.getCode());
        b.setIsbn(dto.getIsbn());
        b.setTitle(dto.getTitle());
        b.setAuthor(dto.getAuthor());
        b.setCategory(dto.getCategory());
        b.setPublisher(dto.getPublisher());
        b.setYear(dto.getYear());
        b.setTotalCopies(dto.getTotalCopies());
        b.setAvailableCopies(dto.getAvailableCopies());
        b.setLocation(dto.getLocation());
        b.setStatus(dto.getStatus() != null ? dto.getStatus() : BookStatus.AVAILABLE);
    }

    private void mapReader(LibraryReaderDTO dto, LibraryReader r) {
        r.setCode(dto.getCode());
        r.setName(dto.getName());
        r.setEmail(dto.getEmail());
        r.setPhone(dto.getPhone());
        r.setType(dto.getType());
        r.setStatus(dto.getStatus() != null ? dto.getStatus() : Status.ACTIVE);
        r.setClassName(dto.getClassName());
        r.setDocumentId(dto.getDocumentId());
        r.setAddress(dto.getAddress());
        r.setBirthDate(dto.getBirthDate());
        r.setGender(dto.getGender());
        r.setProfession(dto.getProfession());
        r.setInstitution(dto.getInstitution());
        r.setGuardianName(dto.getGuardianName());
        r.setGuardianPhone(dto.getGuardianPhone());
        r.setNotes(dto.getNotes());
    }

    private LibraryBookDTO toBookDTO(LibraryBook b) {
        LibraryBookDTO dto = new LibraryBookDTO();
        dto.setId(b.getId());
        dto.setCode(b.getCode());
        dto.setIsbn(b.getIsbn());
        dto.setTitle(b.getTitle());
        dto.setAuthor(b.getAuthor());
        dto.setCategory(b.getCategory());
        dto.setPublisher(b.getPublisher());
        dto.setYear(b.getYear());
        dto.setTotalCopies(b.getTotalCopies());
        dto.setAvailableCopies(b.getAvailableCopies());
        dto.setLocation(b.getLocation());
        dto.setStatus(b.getStatus());
        return dto;
    }

    private LibraryLoanDTO toLoanDTO(LibraryLoan l) {
        LibraryLoanDTO dto = new LibraryLoanDTO();
        dto.setId(l.getId());
        dto.setBookId(l.getBook().getId());
        dto.setBookTitle(l.getBook().getTitle());
        dto.setReaderId(l.getReader().getId());
        dto.setReaderName(l.getReader().getName());
        dto.setLoanDate(l.getLoanDate());
        dto.setDueDate(l.getDueDate());
        dto.setReturnDate(l.getReturnDate());
        dto.setStatus(l.getStatus());
        dto.setRenewalCount(l.getRenewalCount());
        dto.setNotes(l.getNotes());
        return dto;
    }

    private LibraryReaderDTO toReaderDTO(LibraryReader r) {
        LibraryReaderDTO dto = new LibraryReaderDTO();
        dto.setId(r.getId());
        dto.setCode(r.getCode());
        dto.setName(r.getName());
        dto.setEmail(r.getEmail());
        dto.setPhone(r.getPhone());
        dto.setType(r.getType());
        dto.setStatus(r.getStatus());
        dto.setClassName(r.getClassName());
        dto.setDocumentId(r.getDocumentId());
        dto.setAddress(r.getAddress());
        dto.setBirthDate(r.getBirthDate());
        dto.setGender(r.getGender());
        dto.setProfession(r.getProfession());
        dto.setInstitution(r.getInstitution());
        dto.setGuardianName(r.getGuardianName());
        dto.setGuardianPhone(r.getGuardianPhone());
        dto.setNotes(r.getNotes());
        long activeLoans = loanRepository.countByReaderIdAndStatus(r.getId(), LoanStatus.ACTIVE);
        dto.setActiveLoans((int) activeLoans);
        return dto;
    }
}
