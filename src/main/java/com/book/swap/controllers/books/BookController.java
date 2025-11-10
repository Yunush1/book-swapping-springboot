package com.book.swap.controllers.books;

import com.book.swap.models.dto.BookDto;
import com.book.swap.models.dto.CreateBookRequest;
import com.book.swap.services.BookService;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/books")
public class BookController {

    private final BookService bookService;

    @PostMapping
    public ResponseEntity<BookDto> createBook(@Valid @RequestBody CreateBookRequest request,
                                              @RequestHeader("User-Id") String userId) {
        return ResponseEntity.ok(bookService.createBook(request, userId));
    }

    @GetMapping
    public ResponseEntity<List<BookDto>> getAvailableBooks() {
        return ResponseEntity.ok(bookService.getAvailableBooks());
    }

    @GetMapping("/my-books")
    public ResponseEntity<List<BookDto>> getMyBooks(@RequestHeader("User-Id") String userId) {
        return ResponseEntity.ok(bookService.getUserBooks(userId));
    }

    @GetMapping("/search")
    public ResponseEntity<List<BookDto>> searchBooks(@RequestParam String title) {
        return ResponseEntity.ok(bookService.searchBooksByTitle(title));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookDto> getBook(@PathVariable String id) {
        return ResponseEntity.ok(bookService.getBookById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable String id,
                                           @RequestHeader("User-Id") String userId) {
//        bookService.deleteBook(id, userId);
        return ResponseEntity.noContent().build();
    }
}
