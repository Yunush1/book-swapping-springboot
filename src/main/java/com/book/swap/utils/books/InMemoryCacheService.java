package com.book.swap.utils.books;

import com.book.swap.models.entities.DbUsers;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * In-memory implementation of CacheService
 * For production, consider using Redis or other distributed cache
 */
@Slf4j
@Service
public class InMemoryCacheService implements CacheService {

    private final Map<String, DbUsers> sessionCache = new ConcurrentHashMap<>();

    @Override
    public void storeSession(String  sessionId, DbUsers user) {
        if (sessionId == null || user == null) {
            log.warn("Attempted to store null session or user");
            return;
        }
        sessionCache.put(sessionId, user);
        log.debug("Session stored: {}", sessionId);
    }

    @Override
    public DbUsers getSession(String sessionId) {
        if (sessionId == null) {
            return null;
        }
        DbUsers user = sessionCache.get(sessionId);
        log.debug("Session retrieved: {} - Found: {}", sessionId, user != null);
        return user;
    }

    @Override
    public void invalidateSession(String sessionId) {
        if (sessionId == null) {
            return;
        }
        sessionCache.remove(sessionId);
        log.debug("Session invalidated: {}", sessionId);
    }

    @Override
    public boolean sessionExists(String sessionId) {
        return sessionId != null && sessionCache.containsKey(sessionId);
    }
}