package com.als.securityserver.repository;

import com.als.securityserver.entity.UserAuthRoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserAuthRoleRepository extends JpaRepository<UserAuthRoleEntity, Long> {

    List<UserAuthRoleEntity> findAllById_UserPoid(Long userPoid);
}
