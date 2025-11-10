package com.book.swap.utils.books;
import com.book.swap.models.entities.DbUsers;

public interface CacheService {

    /**
     * Store user session in cache
     * @param sessionId Session identifier
     * @param user User entity
     */
    void storeSession(String sessionId, DbUsers user);

    /**
     * Retrieve user session from cache
     * @param sessionId Session identifier
     * @return User entity or null if not found
     */
    DbUsers getSession(String sessionId);

    /**
     * Invalidate user session
     * @param sessionId Session identifier
     */
    void invalidateSession(String sessionId);

    /**
     * Check if session exists
     * @param sessionId Session identifier
     * @return true if session exists, false otherwise
     */
    boolean sessionExists(String sessionId);
}
