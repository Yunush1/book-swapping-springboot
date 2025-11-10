package com.book.swap.models.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenValidationResponse {

    private boolean valid;
    private boolean expired;
    private String message;
    private String email;
    private Long userId;
    private Date expiresAt;
    private String tokenType;
}