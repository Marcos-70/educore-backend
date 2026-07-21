package com.api.educore.service;

import com.api.educore.dto.AuthRequest;
import com.api.educore.dto.AuthResponse;
import com.api.educore.dto.ChangePasswordRequest;
import com.api.educore.dto.RegisterRequest;
import com.api.educore.dto.UpdateProfileRequest;
import com.api.educore.dto.UserDTO;
import com.api.educore.model.User;
import com.api.educore.model.UserRole;
import com.api.educore.repository.UserRepository;
import com.api.educore.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public AuthResponse login(AuthRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Email ou senha invalidos"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Credenciais invalidas");
        }

        if (!user.isActive()) {
            throw new RuntimeException("Conta desativada");
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name(),
                user.getSchool() != null ? user.getSchool().getId() : null);

        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole().name())
                .userId(user.getId())
                .schoolId(user.getSchool() != null ? user.getSchool().getId() : null)
                .schoolName(user.getSchool() != null ? user.getSchool().getName() : null)
                .build();
    }

    public User register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email ja cadastrado");
        }
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("Username ja utilizado");
        }
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilizador nao autenticado"));

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole() != null ? UserRole.valueOf(request.getRole()) : UserRole.SECRETARIO)
                .position(request.getPosition())
                .phone(request.getPhone())
                .gender(request.getGender())
                .school(currentUser.getSchool())
                .active(true)
                .build();
        return userRepository.save(user);
    }

    public void changePassword(ChangePasswordRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilizador nao encontrado"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("Senha atual incorreta");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    public void updateProfile(UpdateProfileRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilizador nao encontrado"));

        user.setFirstName(request.getName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        userRepository.save(user);
    }

    public List<UserDTO> getUsers() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilizador nao encontrado"));
        List<User> users;
        if (currentUser.getSchool() != null) {
            users = userRepository.findBySchoolId(currentUser.getSchool().getId());
        } else {
            users = userRepository.findAllWithSchool();
        }
        return users.stream().map(this::toDTO).toList();
    }

    private UserDTO toDTO(User u) {
        return UserDTO.builder()
                .id(u.getId())
                .firstName(u.getFirstName())
                .lastName(u.getLastName())
                .username(u.getUsername())
                .email(u.getEmail())
                .role(u.getRole() != null ? u.getRole().name() : null)
                .position(u.getPosition())
                .phone(u.getPhone())
                .avatar(u.getAvatar())
                .address(u.getAddress())
                .gender(u.getGender())
                .sexo(u.getSexo() != null ? u.getSexo().name() : null)
                .active(u.isActive())
                .schoolId(u.getSchool() != null ? u.getSchool().getId() : null)
                .schoolName(u.getSchool() != null ? u.getSchool().getName() : null)
                .build();
    }
}
