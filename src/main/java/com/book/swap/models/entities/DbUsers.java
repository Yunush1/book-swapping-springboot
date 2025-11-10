package com.book.swap.models.entities;

import com.book.swap.utils.books.Constants;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Document(collection = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DbUsers {
    @Id
    @MongoId
    private String id;

    @Indexed(unique = true)
    private String email;
    @Indexed(unique = true)
    private String username;
    private String password;
    private String fullName;
    @Indexed(unique = true, sparse = true)
    private String phoneNumber;
    private Address address;

    private String sessionId;
    private LocalDateTime lastLoginAt;
    @Builder.Default
    private boolean enabled = true;
    @Builder.Default
    private Set<Integer> roles = Set.of(Constants.ROLE.USER);

    @Builder.Default
    private boolean active = true;

    @Builder.Default
    private boolean deleted = false;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt;
}
