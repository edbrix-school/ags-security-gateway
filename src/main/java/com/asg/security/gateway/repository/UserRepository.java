package com.asg.security.gateway.repository;

import com.asg.security.gateway.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUserIdIgnoreCaseAndActive(String userId, String active);

    Optional<User> findByEmailIgnoreCaseAndActive(String email, String active);

    @Query("SELECT u FROM User u WHERE LOWER(u.userId) = LOWER(:userId)AND u.active = 'Y' AND u.deleted is NULL")
    Optional<User> findByActiveUserId(@Param("userId") String userId);
}

