package com.book.swap.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties("jwt")
public class JwtProperties {
    String secret;
    Long accessTokenExpiration;
    Long refreshTokenExpiration;
}
