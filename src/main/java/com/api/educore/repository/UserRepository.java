package com.api.educore.repository;

import com.api.educore.model.User;
import com.api.educore.model.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    boolean existsByEmail(String email);
    long countByRole(UserRole role);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.school WHERE u.school.id = :schoolId")
    List<User> findBySchoolId(@Param("schoolId") Long schoolId);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.school")
    List<User> findAllWithSchool();
}
