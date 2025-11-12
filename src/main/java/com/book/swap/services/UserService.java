package com.book.swap.services;

import com.book.swap.models.dto.ApiResponse;
import com.book.swap.models.dto.UserDTO;
import com.book.swap.models.entities.DbUsers;
import com.book.swap.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final MongoTemplate mongoTemplate;
    Logger logger = LoggerFactory.getLogger(UserService.class);

    public UserDTO createUser(UserDTO userDTO) {
        DbUsers dbUsers = DbUsers.builder().email(userDTO.getEmail()).fullName(userDTO.getFullName()).password(userDTO.getPassword()).username(userDTO.getUserName()).phoneNumber(userDTO.getPhoneNumber()).build();
        DbUsers savedUser = userRepository.save(dbUsers);
        return convertToDTO(savedUser);
    }


    public ApiResponse<Page<UserDTO>> getAllUsers(String after, Integer limit, String search, Integer role) {
        int requestedLimit = (limit != null && limit > 0) ? limit : 10;
        int pageLimit = requestedLimit + 1; // Fetch one extra to check if more exist

        Query query = new Query();

        // ✅ Cursor-based pagination
        if (after != null && !after.isEmpty()) {
            try {
                ObjectId afterObjectId = new ObjectId(after);
                query.addCriteria(Criteria.where("_id").gt(afterObjectId));
            } catch (IllegalArgumentException e) {
                logger.warn("Invalid after cursor: {}", after);
            }
        }

        // 🔍 Search filter
        if (search != null && !search.trim().isEmpty()) {
            query.addCriteria(new Criteria().orOperator(
                    Criteria.where("name").regex(search, "i"),
                    Criteria.where("email").regex(search, "i"),
                    Criteria.where("username").regex(search, "i")
            ));
        }

        // 👥 Role filter
        if (role != null && role != 0) {
            query.addCriteria(Criteria.where("roles").in(role));
        }

        // ⚙️ Sort ascending by ObjectId
        query.with(Sort.by(Sort.Direction.ASC, "_id"));
        query.limit(pageLimit);

        List<DbUsers> users = mongoTemplate.find(query, DbUsers.class);

        // 🔄 Check if there are more items
        boolean hasMore = users.size() > requestedLimit;

        // Remove extra item if exists
        if (hasMore) {
            users = users.subList(0, requestedLimit);
        }

        // 🧩 Convert to DTOs
        List<UserDTO> userDTOs = users.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        // 🧾 Wrap in Page object (manual pagination)
        Page<UserDTO> page = new PageImpl<>(userDTOs);

        // ✅ Wrap everything in ApiResponse
        return ApiResponse.<Page<UserDTO>>builder()
                .data(page)
                .message("Users fetched successfully" + (hasMore ? " (more available)" : ""))
                .success(true)
                .build();
    }

    private UserDTO convertToDTO(DbUsers dbUsers) {
        return UserDTO.builder().id(dbUsers.getId()).email(dbUsers.getEmail()).role(dbUsers.getRoles().iterator().next()).fullName(dbUsers.getFullName()).userName(dbUsers.getUsername()).phoneNumber(dbUsers.getPhoneNumber()).build();
    }
}
