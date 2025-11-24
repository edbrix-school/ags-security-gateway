package com.als.securityserver.repository;

import com.als.securityserver.entity.master.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoleRepository extends JpaRepository<RoleEntity, Long> {

    RoleEntity findByUserRolePoid(Long userRolePoid);

    List<RoleEntity> findByUserRolePoidIn(List<Long> userRolePoids);

    boolean existsByUserRoleName(String userRoleName);

    boolean existsByUserRoleId(String userRoleId);

    boolean existsByUserRolePoid(Long userRolePoid);

    boolean existsByUserRoleIdAndUserRolePoidNot(String userRoleId, Long userRolePoid);

}
