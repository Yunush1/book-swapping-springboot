package com.book.swap.repository;

import com.book.swap.models.entities.DbUsers;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<DbUsers, String> {
    Optional<DbUsers> findByUsername(String username);

    Optional<DbUsers> findByEmail(String email);
    Optional<DbUsers> findByEmailOrUsername(String email, String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    List<DbUsers> findByIdGreaterThanOrderByIdAsc(String after, Pageable pageable);
}
