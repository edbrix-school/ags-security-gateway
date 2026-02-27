package com.asg.security.gateway.repository;


import com.asg.security.gateway.entity.UsersCompanyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UsersCompanyRepository extends JpaRepository<UsersCompanyEntity, Long> {

    @Query("SELECT c FROM UsersCompanyEntity c " +
            "JOIN Company comp ON c.id.companyPoid = comp.companyPoid " +
            "WHERE c.id.userPoid = :userPoid AND (comp.active = 'Y' OR comp.active IS NULL) AND (comp.deleted = 'N' OR comp.deleted IS NULL)")
    List<UsersCompanyEntity> findCompanyAccess(@Param("userPoid") Long userPoid);
}
