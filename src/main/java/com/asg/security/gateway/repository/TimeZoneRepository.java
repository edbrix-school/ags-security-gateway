package com.asg.security.gateway.repository;

import com.asg.security.gateway.entity.TimeZoneEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TimeZoneRepository extends JpaRepository<TimeZoneEntity, Long> {
    TimeZoneEntity findByTimezoneId(Long timezoneId);
}
