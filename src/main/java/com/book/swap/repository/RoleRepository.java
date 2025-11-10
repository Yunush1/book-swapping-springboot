package com.book.swap.repository;

import com.book.swap.models.entities.DbRole;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends MongoRepository<DbRole, String> {
    Optional<DbRole> findByName(String name);

    boolean existsByName(String name);
}
