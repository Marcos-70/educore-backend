package com.api.educore.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank(message = "Nome e obrigatorio")
    private String name;

    @NotBlank(message = "Email e obrigatorio")
    @Email(message = "Formato de email invalido")
    private String email;

    @NotBlank(message = "Senha e obrigatoria")
    @Size(min = 8, message = "A senha deve ter pelo menos 8 caracteres")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).*$", message = "A senha deve conter pelo menos uma letra maiuscula, uma minuscula e um numero")
    private String password;

    private String role;
}
