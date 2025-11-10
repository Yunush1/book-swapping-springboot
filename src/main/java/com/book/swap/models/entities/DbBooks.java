package com.book.swap.models.entities;

import com.book.swap.models.enums.BookCondition;
import com.book.swap.models.enums.BookStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Document(collection = "books")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DbBooks {
    @Id
    private String id;

    @Indexed
    private String title;

    private String description;
    private String imageUrl;
    private String author;
    private String publisher;
    private String isbn;

    @Indexed
    private String categoryId;

    @Indexed
    private String ownerId;

    @Builder.Default
    private Set<String> languages = new HashSet<>();

    @Builder.Default
    private BookCondition condition = BookCondition.GOOD;

    @Builder.Default
    private BookStatus status = BookStatus.AVAILABLE;

    @Builder.Default
    private boolean active = true;

    @Builder.Default
    private boolean deleted = false;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt;
}
