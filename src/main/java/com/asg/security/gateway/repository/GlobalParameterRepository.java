package com.asg.security.gateway.repository;

import com.asg.security.gateway.entity.GlobalParameterEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface GlobalParameterRepository extends JpaRepository<GlobalParameterEntity, Long> {

    @Query("""
              SELECT g.parameterValue
              FROM GlobalParameterEntity g
              WHERE g.parameterType = :parameterType
                AND g.parameterName = :parameterName
            """)
    Integer findParameterValueByParameterName(
            @Param("parameterType") String parameterType,
            @Param("parameterName") String parameterName
    );
}
