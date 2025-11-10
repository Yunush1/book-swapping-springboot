package com.book.swap.services.serviceImpl;

import com.book.swap.models.dto.BookDto;
import com.book.swap.models.dto.CreateBookRequest;

import java.util.List;


public interface BookServiceImpl {
    BookDto createBook(CreateBookRequest request, String userId);

    List<BookDto> getAvailableBooks();

    List<BookDto> getUserBooks(String userId);
    List<BookDto> getUserBooksByOwnerId(String ownerId);

    List<BookDto> searchBooksByTitle(String search);

    BookDto getBookById(String bookId);
}
