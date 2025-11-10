package com.book.swap.utils.books;

import com.book.swap.models.dto.BookDto;
import com.book.swap.models.dto.CreateBookRequest;
import com.book.swap.models.entities.DbBooks;
import com.book.swap.models.entities.DbCategory;
import com.book.swap.models.entities.DbUsers;
import com.book.swap.repository.CategoryRepository;
import com.book.swap.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BooksUtils {

    private CategoryRepository categoryRepository;
    private UserRepository userRepository;

    public BookDto convertToDTO(DbBooks book) {
        DbCategory category = categoryRepository.findById(book.getCategoryId()).orElse(null);
        DbUsers owner = userRepository.findById(book.getOwnerId()).orElseThrow(() -> new RuntimeException("User not found"));

        return BookDto.builder()
                .id(book.getId())
                .title(book.getTitle())
                .description(book.getDescription())
                .imageUrl(book.getImageUrl())
                .author(book.getAuthor())
                .publisher(book.getPublisher())
                .isbn(book.getIsbn())
                .categoryId(book.getCategoryId())
                .categoryName(category != null ? category.getTitle() : null)
                .ownerId(book.getOwnerId())
                .ownerName(owner != null ? owner.getFullName() : null)
                .languages(book.getLanguages())
                .condition(book.getCondition())
                .status(book.getStatus())
                .build();
    }

    public DbBooks convertDTOToBooks(CreateBookRequest bookDto, String userId) {
         return DbBooks.builder()
                .title(bookDto.getTitle())
                .author(bookDto.getAuthor())
                .description(bookDto.getDescription())
                .condition(bookDto.getCondition())
                .languages(bookDto.getLanguages())
                .categoryId(bookDto.getCategoryId())
                .build();
    }
}
