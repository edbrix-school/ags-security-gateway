package com.asg.security.gateway.service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class CacheService {
    
    private final Cache<String, Cache<String, Object>> cacheRegistry = CacheBuilder.newBuilder()
            .maximumSize(100)
            .build();
    
    public void put(String cacheName, String key, Object value, long ttlMinutes) {
        Cache<String, Object> cache = getOrCreateCache(cacheName, ttlMinutes);
        cache.put(key, value);
    }
    
    public <T> T get(String cacheName, String key, Class<T> type) {
        Cache<String, Object> cache = cacheRegistry.getIfPresent(cacheName);
        if (cache == null) return null;
        
        Object value = cache.getIfPresent(key);
        return type.isInstance(value) ? type.cast(value) : null;
    }
    
    public void evict(String cacheName, String key) {
        Cache<String, Object> cache = cacheRegistry.getIfPresent(cacheName);
        if (cache != null) {
            cache.invalidate(key);
        }
    }
    
    public void clearCache(String cacheName) {
        Cache<String, Object> cache = cacheRegistry.getIfPresent(cacheName);
        if (cache != null) {
            cache.invalidateAll();
        }
    }
    
    private Cache<String, Object> getOrCreateCache(String cacheName, long ttlMinutes) {
        Cache<String, Object> cache = cacheRegistry.getIfPresent(cacheName);
        if (cache == null) {
            cache = CacheBuilder.newBuilder()
                    .expireAfterWrite(ttlMinutes, TimeUnit.MINUTES)
                    .maximumSize(1000)
                    .build();
            cacheRegistry.put(cacheName, cache);
        }
        return cache;
    }
}