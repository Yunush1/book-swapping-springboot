package com.book.swap.repository;

import com.book.swap.models.entities.DbCategory;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends MongoRepository<DbCategory, String> {
    List<DbCategory> findByTitle(String title);
    List<DbCategory> findByActiveAndDeletedIsFalse(boolean active);
}
