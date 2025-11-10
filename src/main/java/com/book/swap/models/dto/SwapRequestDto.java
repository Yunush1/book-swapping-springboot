package com.book.swap.models.dto;

import com.book.swap.models.enums.SwapStatus;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SwapRequestDto {
    private String id;
    private String requesterId;
    private String requesterName;
    private String ownerId;
    private String ownerName;
    private BookDto requestedBook;
    private BookDto offeredBook;
    private String message;
    private SwapStatus status;
    private LocalDateTime createdAt;
}
