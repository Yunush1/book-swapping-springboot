package com.book.swap.models.entities;

import com.book.swap.utils.books.Constants;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.util.HashSet;
import java.util.Set;

@Document(collection = "Roles")
@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class DbRole {
    @Id
    @MongoId
    private String id;
    private int role = Constants.ROLE.USER;
    private String name;
    private Set<DbPermission> permissions = new HashSet<>();
}
