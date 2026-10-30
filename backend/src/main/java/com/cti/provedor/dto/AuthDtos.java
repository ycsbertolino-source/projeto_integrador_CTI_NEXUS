package com.cti.provedor.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public final class AuthDtos {

    private AuthDtos() {}  // só agrupa os formatos

    public record CadastroRequest(
            @NotBlank(message = "Informe o nome") @Size(max = 120) String nome,
            @NotBlank(message = "Informe o e-mail") @Email(message = "E-mail inválido")
            @Size(max = 160) String email,
            @NotBlank(message = "Informe a senha")
            @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,72}$",  // 8 a 72, letra e número
                    message = "A senha deve ter de 8 a 72 caracteres, com letras e números") String senha,
            @Size(max = 30) String matricula,  // opcional
            @Size(max = 60) String turma) {}   // opcional

    public record LoginRequest(
            @NotBlank(message = "Informe o e-mail") String email,
            @NotBlank(message = "Informe a senha") String senha) {}

    public record LoginResponse(String token, String tipo, long expiraEmSegundos, UsuarioDto usuario) {}

    public record MensagemResponse(String mensagem) {}
}
