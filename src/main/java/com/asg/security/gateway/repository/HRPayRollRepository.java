package com.asg.security.gateway.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.StoredProcedureQuery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
public class HRPayRollRepository {
    @PersistenceContext
    private EntityManager em;

    public String SyncHRDataSP() {

        StoredProcedureQuery query = em.createStoredProcedureQuery("SYNC_HR_PRODUCTION_TO_PAYROLL");
        query.registerStoredProcedureParameter("P_STATUS", String.class, ParameterMode.OUT);
        query.execute();

        return (String) query.getOutputParameterValue("P_STATUS");
    }
}
