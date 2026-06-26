package com.asg.security.gateway.service;

import com.asg.security.gateway.dto.MenuItemDto;
import com.asg.security.gateway.repository.MenuRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MenuCacheService {

    private static final long REFRESH_MS  = 5  * 60 * 1000L;
    private static final long IDLE_MS     = 30 * 60 * 1000L;

    private record CacheEntry(List<MenuItemDto> menus, Instant fetchedAt, Instant lastAccessTime) {}

    private final ConcurrentHashMap<Long, CacheEntry> cache = new ConcurrentHashMap<>();

    @Autowired
    private MenuRepository menuRepository;

    public List<MenuItemDto> getMenus(Long userPoid) {
        Instant now = Instant.now();
        CacheEntry entry = cache.get(userPoid);

        if (entry != null) {
            boolean idle  = entry.lastAccessTime().plusMillis(IDLE_MS).isBefore(now);
            boolean stale = entry.fetchedAt().plusMillis(REFRESH_MS).isBefore(now);

            if (idle) {
                cache.remove(userPoid);
                log.info("Menu cache idle-evicted for userPoid={}", userPoid);
            } else if (!stale) {
                cache.put(userPoid, new CacheEntry(entry.menus(), entry.fetchedAt(), now));
                log.debug("Menu cache hit for userPoid={}", userPoid);
                return entry.menus();
            }
            // stale but not idle: fall through and re-fetch below
        }

        log.info("Fetching menu from DB for userPoid={}", userPoid);
        List<MenuItemDto> menus = fetchFromDb(userPoid);
        log.info("DB returned {} menu items for userPoid={}", menus.size(), userPoid);
        cache.put(userPoid, new CacheEntry(menus, now, now));
        return menus;
    }

    public void evict(Long userPoid) {
        cache.remove(userPoid);
        log.info("Menu cache evicted for userPoid={}", userPoid);
    }

    private List<MenuItemDto> fetchFromDb(Long userPoid) {
        List<Object[]> results = menuRepository.findMenuItemsByUserPoid(userPoid);
        List<MenuItemDto> menuItems = results.stream().map(row -> new MenuItemDto(
                (String) row[0],
                (String) row[1],
                ((Number) row[2]).intValue(),
                (String) row[3],
                (String) row[4],
                (String) row[5],
                (String) row[6],
                (String) row[7],
                row[8] != null ? row[8].toString() : null,
                (String) row[9],
                new ArrayList<>()
        )).collect(Collectors.toList());
        return buildMenuHierarchy(menuItems);
    }

    private List<MenuItemDto> buildMenuHierarchy(List<MenuItemDto> menuItems) {
        Map<Integer, List<MenuItemDto>> levels = menuItems.stream()
                .collect(Collectors.groupingBy(MenuItemDto::getMenuLevel));

        List<MenuItemDto> level0 = levels.getOrDefault(0, List.of());
        List<MenuItemDto> level1 = levels.getOrDefault(1, List.of());
        List<MenuItemDto> level2 = levels.getOrDefault(2, List.of());

        Map<String, List<MenuItemDto>> level1Map = level1.stream()
                .collect(Collectors.groupingBy(MenuItemDto::getMenuGroup));
        Map<String, List<MenuItemDto>> level2Map = level2.stream()
                .collect(Collectors.groupingBy(MenuItemDto::getMenuGroup));

        for (MenuItemDto l1 : level1) {
            l1.getChildren().addAll(level2Map.getOrDefault(l1.getMenuId(), List.of()));
        }
        for (MenuItemDto l0 : level0) {
            l0.getChildren().addAll(level1Map.getOrDefault(l0.getMenuId(), List.of()));
        }
        return level0;
    }
}
