package com.asg.security.gateway.repository;

import com.asg.security.gateway.entity.UserAuthRoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserAuthRoleRepository extends JpaRepository<UserAuthRoleEntity, Long> {

    List<UserAuthRoleEntity> findAllById_UserPoid(Long userPoid);
}
