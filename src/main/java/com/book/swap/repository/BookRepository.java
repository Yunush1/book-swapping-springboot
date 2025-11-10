package com.book.swap.repository;

import com.book.swap.models.entities.DbBooks;
import com.book.swap.models.enums.BookStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.awt.print.Book;
import java.awt.print.Pageable;
import java.util.List;

@Repository
public interface BookRepository extends MongoRepository<DbBooks, String> {

    List<DbBooks> findByCategoryId(String categoryId);
    List<DbBooks> findByOwnerId(String ownerId);
    List<DbBooks> findByStatus(BookStatus status);
    List<DbBooks> findByOwnerIdAndStatus(String ownerId, BookStatus status);
    List<DbBooks> findByTitleContainingIgnoreCase(String title);
    List<DbBooks> findByAuthorContainingIgnoreCase(String author);
    List<DbBooks> findByAuthorContainsOrTitleContainsOrDescriptionContains(String author, String title, String description);
}
