package com.book.swap.repository;

import com.book.swap.models.entities.DbPermission;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface PermissionRepository extends MongoRepository<DbPermission, String> {
    Optional<DbPermission> findByName(String name);
    boolean existsByName(String name);
}
