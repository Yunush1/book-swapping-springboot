package com.book.swap.models.entities;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;


@Document(collection = "categories")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DbCategory {
    @Id
    @MongoId
    private String id;

    @Indexed(unique = true)
    private String title;

    private String imageUrl;

    private String description;

    @Builder.Default
    private boolean active = true;

    @Builder.Default
    private boolean deleted = false;


//    private List<DbBooks> dbBooks;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt;
}
