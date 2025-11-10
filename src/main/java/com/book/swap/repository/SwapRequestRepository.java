package com.book.swap.repository;

import com.book.swap.models.entities.DbSwapRequest;
import com.book.swap.models.enums.SwapStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SwapRequestRepository extends MongoRepository<DbSwapRequest, String> {
    List<DbSwapRequest> findByRequesterId(String requesterId);
    List<DbSwapRequest> findByOwnerId(String ownerId);
    List<DbSwapRequest> findByStatus(SwapStatus status);
    List<DbSwapRequest> findByRequesterIdAndStatus(String requesterId, SwapStatus status);
    List<DbSwapRequest> findByOwnerIdAndStatus(String ownerId, SwapStatus status);

}
