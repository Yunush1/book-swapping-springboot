package com.book.swap.services;

import com.book.swap.models.dto.SwapRequestDto;
import com.book.swap.models.entities.DbBooks;
import com.book.swap.models.entities.DbSwapRequest;
import com.book.swap.models.enums.BookStatus;
import com.book.swap.models.enums.SwapStatus;
import com.book.swap.repository.BookRepository;
import com.book.swap.repository.SwapRequestRepository;
import com.book.swap.utils.books.BooksUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SwapService {
    private final SwapRequestRepository swapRequestRepository;
    private final BookRepository bookRepository;
    private final BookService bookService;
    private final BooksUtils booksUtils;
    public SwapRequestDto createSwapRequest(String requesterId, String requestedBookId,
                                            String offeredBookId, String message) {
        DbBooks requestedBook = bookRepository.findById(requestedBookId)
                .orElseThrow(() -> new RuntimeException("Requested book not found"));

        DbBooks offeredBook = bookRepository.findById(offeredBookId)
                .orElseThrow(() -> new RuntimeException("Offered book not found"));

        if (!offeredBook.getOwnerId().equals(requesterId)) {
            throw new RuntimeException("You can only offer your own books");
        }

        if (requestedBook.getStatus() != BookStatus.AVAILABLE) {
            throw new RuntimeException("Book is not available for swap");
        }

        DbSwapRequest swapRequest = DbSwapRequest.builder()
                .requesterId(requesterId)
                .ownerId(requestedBook.getOwnerId())
                .requestedBookId(requestedBookId)
                .offeredBookId(offeredBookId)
                .message(message)
                .build();

        DbSwapRequest saved = swapRequestRepository.save(swapRequest);
        return convertToDTO(saved);
    }

    @Transactional
    public SwapRequestDto acceptSwapRequest(String swapRequestId, String userId) {
        DbSwapRequest swapRequest = swapRequestRepository.findById(swapRequestId)
                .orElseThrow(() -> new RuntimeException("Swap request not found"));

        if (!swapRequest.getOwnerId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        if (swapRequest.getStatus() != SwapStatus.PENDING) {
            throw new RuntimeException("Swap request is not pending");
        }

        swapRequest.setStatus(SwapStatus.ACCEPTED);
        swapRequest.setRespondedAt(LocalDateTime.now());

        DbBooks requestedBook = bookRepository.findById(swapRequest.getRequestedBookId())
                .orElseThrow(() -> new RuntimeException("Book not found"));
        requestedBook.setStatus(BookStatus.IN_SWAP);
        bookRepository.save(requestedBook);

        DbBooks offeredBook = bookRepository.findById(swapRequest.getOfferedBookId())
                .orElseThrow(() -> new RuntimeException("Book not found"));
        offeredBook.setStatus(BookStatus.IN_SWAP);
        bookRepository.save(offeredBook);

        DbSwapRequest saved = swapRequestRepository.save(swapRequest);
        return convertToDTO(saved);
    }

    public SwapRequestDto rejectSwapRequest(String swapRequestId, String userId) {
        DbSwapRequest swapRequest = swapRequestRepository.findById(swapRequestId)
                .orElseThrow(() -> new RuntimeException("Swap request not found"));

        if (!swapRequest.getOwnerId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        swapRequest.setStatus(SwapStatus.REJECTED);
        swapRequest.setRespondedAt(LocalDateTime.now());

        DbSwapRequest saved = swapRequestRepository.save(swapRequest);
        return convertToDTO(saved);
    }

    public List<SwapRequestDto> getReceivedRequests(String userId) {
        return swapRequestRepository.findByOwnerId(userId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<SwapRequestDto> getSentRequests(String userId) {
        return swapRequestRepository.findByRequesterId(userId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private SwapRequestDto convertToDTO(DbSwapRequest swapRequest) {
        return SwapRequestDto.builder()
                .id(swapRequest.getId())
                .requesterId(swapRequest.getRequesterId())
                .ownerId(swapRequest.getOwnerId())
                .requestedBook(bookService.getBookById(swapRequest.getRequestedBookId()))
                .offeredBook(bookService.getBookById(swapRequest.getOfferedBookId()))
                .message(swapRequest.getMessage())
                .status(swapRequest.getStatus())
                .createdAt(swapRequest.getCreatedAt())
                .build();
    }
}
