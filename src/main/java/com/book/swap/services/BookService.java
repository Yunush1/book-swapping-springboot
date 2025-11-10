package com.book.swap.services;

import com.book.swap.models.dto.BookDto;
import com.book.swap.models.dto.CreateBookRequest;
import com.book.swap.models.entities.DbBooks;
import com.book.swap.models.enums.BookStatus;
import com.book.swap.repository.BookRepository;
import com.book.swap.repository.CategoryRepository;
import com.book.swap.repository.UserRepository;
import com.book.swap.services.serviceImpl.BookServiceImpl;
import com.book.swap.utils.books.BooksUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookService implements BookServiceImpl {
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final BooksUtils booksUtils;
    @Override
    public BookDto createBook(CreateBookRequest request, String userId) {
        DbBooks book = DbBooks.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .imageUrl(request.getImageUrl())
                .author(request.getAuthor())
                .publisher(request.getPublisher())
                .isbn(request.getIsbn())
                .categoryId(request.getCategoryId())
                .ownerId(userId)
                .languages(request.getLanguages())
                .condition(request.getCondition())
                .build();
        DbBooks books = bookRepository.save(book);
        return booksUtils.convertToDTO(books);
    }

    @Override
    public List<BookDto> getAvailableBooks() {
        return bookRepository.findByStatus(BookStatus.AVAILABLE).stream()
                .map(booksUtils::convertToDTO)
                .toList();
    }

    @Override
    public List<BookDto> getUserBooks(String userId) {
        return List.of();
    }

    @Override
    public List<BookDto> getUserBooksByOwnerId(String ownerId) {
        return  bookRepository.findByOwnerId(ownerId)
                .stream()
                .map(booksUtils::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookDto> searchBooksByTitle(String search) {
        return bookRepository.findByTitleContainingIgnoreCase(search)
                .stream()
                .map(booksUtils::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public BookDto getBookById(String bookId) {
        DbBooks book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found"));
        return booksUtils.convertToDTO(book);
    }


}
