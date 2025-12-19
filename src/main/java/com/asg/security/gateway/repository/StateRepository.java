package com.asg.security.gateway.repository;

import com.asg.security.gateway.entity.State;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StateRepository extends JpaRepository<State, Long> {
    Boolean existsByCountryPoidAndStatePoid(Long countryPoid,Long statePoid);
    State findByCountryPoidAndStatePoid(Long countryPoid,Long statePoid);
}
