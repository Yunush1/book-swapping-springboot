package com.book.swap.models.entities;
import com.book.swap.models.enums.SwapStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.time.LocalDateTime;

@Document(collection = "swap_requests")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DbSwapRequest {
    @Id
    @MongoId
    private String id;

    @Indexed
    private String requesterId;

    @Indexed
    private String ownerId;

    @Indexed
    private String requestedBookId;

    @Indexed
    private String offeredBookId;

    private String message;

    @Builder.Default
    private SwapStatus status = SwapStatus.PENDING;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime respondedAt;
    private LocalDateTime completedAt;
}
