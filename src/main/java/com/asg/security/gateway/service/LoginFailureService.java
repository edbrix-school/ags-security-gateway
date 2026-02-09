package com.asg.security.gateway.service;

import com.asg.security.gateway.entity.User;
import com.asg.security.gateway.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoginFailureService {

    private final CacheService cacheService;
    private final UserRepository userRepository;

    public LoginFailureService(
            CacheService cacheService,
            UserRepository userRepository
    ) {
        this.cacheService = cacheService;
        this.userRepository = userRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int handleFailedLogin(User user) {

        // Stop counting if already locked
        if ("Y".equalsIgnoreCase(user.getUserLocked())) {
            return 5;
        }

        String cacheKey = "failedAttempts_" + user.getUserId();
        Integer attempts = cacheService.get("loginCache", cacheKey, Integer.class);
        attempts = (attempts != null ? attempts : 0) + 1;

        cacheService.put("loginCache", cacheKey, attempts, 30);

        if (attempts >= 5) {
            user.setUserLocked("Y");
            user.setUserLockedReason(
                    "Account locked due to 5 consecutive failed login attempts"
            );
            userRepository.saveAndFlush(user);
        }

        return attempts;
    }
}


