package com.book.swap.models.entities;

import com.book.swap.utils.books.Constants;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

@Document(collection = "Permissions")
@Data
@RequiredArgsConstructor
public class DbPermission {
    @Id
    @MongoId
    private String id;
    private String description;

    private int permission = Constants.PERMISSION.CREATE_USER;
    private String name;
}
