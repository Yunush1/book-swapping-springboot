package com.book.swap.models.dto;
import com.book.swap.models.enums.BookCondition;
import com.book.swap.models.enums.BookStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookDto {
    private String id;
    private String title;
    private String description;
    private String imageUrl;
    private String author;
    private String publisher;
    private String isbn;
    private String categoryId;
    private String categoryName;
    private String ownerId;
    private String ownerName;
    private Set<String> languages;
    private BookCondition condition;
    private BookStatus status;
}
