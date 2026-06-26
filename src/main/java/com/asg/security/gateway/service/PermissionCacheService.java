package com.asg.security.gateway.service;

import com.asg.security.gateway.dto.PermissionDto;
import com.asg.security.gateway.repository.PermissionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class PermissionCacheService {

    private static final long REFRESH_MS = 5  * 60 * 1000L;
    private static final long IDLE_MS    = 30 * 60 * 1000L;

    private record CacheEntry(List<PermissionDto> permissions, Instant fetchedAt, Instant lastAccessTime) {}

    private final ConcurrentHashMap<String, CacheEntry> cache = new ConcurrentHashMap<>();

    @Autowired
    private PermissionRepository permissionRepository;

    public List<PermissionDto> getPermissions(String userId) throws SQLException {
        Instant now = Instant.now();
        CacheEntry entry = cache.get(userId);

        if (entry != null) {
            boolean idle  = entry.lastAccessTime().plusMillis(IDLE_MS).isBefore(now);
            boolean stale = entry.fetchedAt().plusMillis(REFRESH_MS).isBefore(now);

            if (idle) {
                cache.remove(userId);
                log.info("Permission cache idle-evicted for userId={}", userId);
            } else if (!stale) {
                cache.put(userId, new CacheEntry(entry.permissions(), entry.fetchedAt(), now));
                log.debug("Permission cache hit for userId={}", userId);
                return entry.permissions();
            }
            // stale but not idle: fall through and re-fetch below
        }

        log.info("Fetching permissions from DB for userId={}", userId);
        List<PermissionDto> permissions = permissionRepository.getUserPermissions(userId);
        log.info("DB returned {} permissions for userId={}", permissions.size(), userId);
        cache.put(userId, new CacheEntry(permissions, now, now));
        return permissions;
    }

    public void evict(String userId) {
        cache.remove(userId);
        log.info("Permission cache evicted for userId={}", userId);
    }
}
