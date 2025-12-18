package com.asg.security.gateway.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class MenuRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public List<Object[]> findMenuItemsByUserPoid(Long userPoid) {
        String sql = """
                  SELECT MENU_ID, MENU_NAME, MENU_LEVEL, MENU_GROUP, TASKFLOW_URL, USER_ID, DOC_TYPE, MODULE_ID, HIDE_IN_MAIN_MENU
                  FROM(
                        -- LEVEL 0: Modules
                        SELECT DISTINCT MM.MODULE_ID MENU_ID, MM.MODULE_NAME MENU_NAME, 0 MENU_LEVEL, '' MENU_GROUP, '' TASKFLOW_URL,
                                        UPPER(UM.USER_ID) USER_ID, '' DOC_TYPE, DM.MODULE_ID, MM.SEQNO, NULL HIDE_IN_MAIN_MENU
                        FROM GLOBAL_MODULE_MASTER MM
                        INNER JOIN GLOBAL_DOC_MASTER DM 
                          ON MM.MODULE_ID = DM.MODULE_ID AND DOC_TYPE <> 'Dashboards' AND NVL(DM.ACTIVE,'Y') = 'Y'
                        INNER JOIN GLOBAL_USER_ROLES_RIGHTS_DTL RD 
                          ON DM.DOC_ID = RD.DOC_ID
                        INNER JOIN GLOBAL_USERS_AUTH_ROLES_DTL UR 
                          ON UR.USER_ROLE_POID = RD.USER_ROLE_POID AND (UR.EXPIRY_DATE IS NULL OR UR.EXPIRY_DATE >= TRUNC(SYSDATE))
                        INNER JOIN GLOBAL_USERS UM 
                          ON UM.USER_POID = UR.USER_POID
                        WHERE UR.USER_POID = ?1
                          AND INSTR(NVL(RD.RIGHTS,'000000'), '1') > 0
                          AND EXISTS (
                                SELECT 1 FROM GLOBAL_DOC_MASTER DM2
                                INNER JOIN GLOBAL_USER_ROLES_RIGHTS_DTL RD2 ON DM2.DOC_ID = RD2.DOC_ID
                                INNER JOIN GLOBAL_USERS_AUTH_ROLES_DTL UR2 ON UR2.USER_ROLE_POID = RD2.USER_ROLE_POID AND (UR2.EXPIRY_DATE IS NULL OR UR2.EXPIRY_DATE >= TRUNC(SYSDATE))
                                WHERE DM2.MODULE_ID = MM.MODULE_ID AND UR2.USER_POID = ?1
                                  AND INSTR(NVL(RD2.RIGHTS,'000000'), '1') > 0 AND NVL(DM2.ACTIVE,'Y') = 'Y' AND DM2.DOC_TYPE <> 'Dashboards'
                          )
                        UNION ALL
                        -- LEVEL 1: Document Types
                        SELECT DISTINCT TO_CHAR(MM.MODULE_ID + DT.CONST_NO) MENU_ID, DT.CONST_NAME MENU_NAME, 1 MENU_LEVEL, 
                                        MM.MODULE_ID MENU_GROUP, '' TASKFLOW_URL, UPPER(UM.USER_ID) USER_ID, '' DOC_TYPE, DM.MODULE_ID, MM.SEQNO, NULL HIDE_IN_MAIN_MENU
                        FROM GLOBAL_MODULE_MASTER MM
                        INNER JOIN GLOBAL_DOC_MASTER DM 
                          ON MM.MODULE_ID = DM.MODULE_ID AND DOC_TYPE <> 'Dashboards' AND NVL(DM.ACTIVE,'Y') = 'Y'
                        INNER JOIN CONST_DOC_TYPES DT ON DM.DOC_TYPE = DT.CONST_NAME AND DM.DOC_TYPE <> 'Settings'
                        INNER JOIN GLOBAL_USER_ROLES_RIGHTS_DTL RD ON DM.DOC_ID = RD.DOC_ID
                        INNER JOIN GLOBAL_USERS_AUTH_ROLES_DTL UR 
                          ON UR.USER_ROLE_POID = RD.USER_ROLE_POID AND (UR.EXPIRY_DATE IS NULL OR UR.EXPIRY_DATE >= TRUNC(SYSDATE))
                        INNER JOIN GLOBAL_USERS UM ON UM.USER_POID = UR.USER_POID
                        WHERE UR.USER_POID = ?1 AND INSTR(NVL(RD.RIGHTS,'000000'), '1') > 0
                          AND EXISTS (
                                SELECT 1 FROM GLOBAL_DOC_MASTER DM3
                                INNER JOIN GLOBAL_USER_ROLES_RIGHTS_DTL RD3 ON DM3.DOC_ID = RD3.DOC_ID
                                INNER JOIN GLOBAL_USERS_AUTH_ROLES_DTL UR3 ON UR3.USER_ROLE_POID = RD3.USER_ROLE_POID AND (UR3.EXPIRY_DATE IS NULL OR UR3.EXPIRY_DATE >= TRUNC(SYSDATE))
                                WHERE DM3.MODULE_ID = MM.MODULE_ID AND DM3.DOC_TYPE = DT.CONST_NAME AND UR3.USER_POID = ?1
                                  AND INSTR(NVL(RD3.RIGHTS,'000000'), '1') > 0 AND NVL(DM3.ACTIVE,'Y') = 'Y' AND DM3.DOC_TYPE <> 'Dashboards'
                          )
                        UNION ALL
                        -- LEVEL 2: Documents / Reports
                        SELECT DISTINCT DM.DOC_ID AS MENU_ID, DM.DOC_SHORT_NAME AS MENU_NAME, 2 MENU_LEVEL, 
                                        TO_CHAR(MM.MODULE_ID + DT.CONST_NO) AS MENU_GROUP, TASKFLOW_URL, 
                                        UPPER(UM.USER_ID) USER_ID, DM.DOC_TYPE, DM.MODULE_ID, DM.SEQNO, NVL(DM.HIDE_IN_MAIN_MENU,'N') HIDE_IN_MAIN_MENU
                        FROM GLOBAL_DOC_MASTER DM
                        INNER JOIN GLOBAL_MODULE_MASTER MM ON DM.MODULE_ID = MM.MODULE_ID
                        INNER JOIN CONST_DOC_TYPES DT ON DM.DOC_TYPE = DT.CONST_NAME AND DM.DOC_TYPE <> 'Settings'
                        INNER JOIN GLOBAL_USER_ROLES_RIGHTS_DTL RD ON DM.DOC_ID = RD.DOC_ID
                        INNER JOIN GLOBAL_USERS_AUTH_ROLES_DTL UR ON UR.USER_ROLE_POID = RD.USER_ROLE_POID AND (UR.EXPIRY_DATE IS NULL OR UR.EXPIRY_DATE >= TRUNC(SYSDATE))
                        INNER JOIN GLOBAL_USERS UM ON UM.USER_POID = UR.USER_POID
                        WHERE UR.USER_POID = ?1 AND DOC_TYPE <> 'Dashboards' AND INSTR(NVL(RD.RIGHTS,'000000'), '1') > 0
                          AND NVL(DM.ACTIVE,'Y') = 'Y'
                  )
                  ORDER BY SEQNO
                """;

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter(1, userPoid);
        return query.getResultList(); // returns List<Object[]>
    }
}
