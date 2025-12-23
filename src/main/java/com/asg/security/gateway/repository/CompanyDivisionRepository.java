package com.asg.security.gateway.repository;

import com.asg.security.gateway.entity.CompanyDivisionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CompanyDivisionRepository extends JpaRepository<CompanyDivisionEntity, Long> {

    List<CompanyDivisionEntity> findById_CompanyPoid(Long companyPoid);
}


