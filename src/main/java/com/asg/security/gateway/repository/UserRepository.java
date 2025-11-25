package com.asg.security.gateway.repository;

import com.asg.security.gateway.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUserIdIgnoreCaseAndActive(String userId, String active);

    Optional<User> findByUserIdIgnoreCase(String userId);

    Optional<User> findByEmailIgnoreCaseAndActive(String email, String active);
}

