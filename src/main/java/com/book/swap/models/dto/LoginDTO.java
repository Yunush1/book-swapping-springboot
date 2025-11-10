package com.book.swap.models.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginDTO {
    @NotBlank(message = "Identifier (email/username) cannot be empty")
    private String identifier;

    @NotBlank(message = "Password cannot be empty")
    private String password;
}
