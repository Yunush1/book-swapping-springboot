package com.book.swap.models.dto;


import com.book.swap.models.enums.BookCondition;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.Set;

@Data
@RequiredArgsConstructor
public class CreateBookRequest {

    private String title;

    private String description;
    private String imageUrl;


    private String author;

    private String publisher;
    private String isbn;


    private String categoryId;

    private Set<String> languages;
    private BookCondition condition;
}
