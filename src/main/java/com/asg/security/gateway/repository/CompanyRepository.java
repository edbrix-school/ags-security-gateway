package com.asg.security.gateway.repository;

import com.asg.security.gateway.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long>, JpaSpecificationExecutor<Company> {

    Company findByCompanyPoid(Long companyPoid);

    @Query("SELECT c.countryId FROM Company c WHERE c.companyPoid = :companyPoid")
    String findCountryIdByCompanyPoid(@Param("companyPoid") Long companyPoid);

    @Query("SELECT c.countryCode FROM Country c WHERE c.countryPoid = :countryPoid")
    String findCountryCodeByCountryPoid(@Param("countryPoid") Long countryPoid);
}
