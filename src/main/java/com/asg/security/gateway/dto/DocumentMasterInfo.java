package com.asg.security.gateway.dto;

import lombok.Builder;
import lombok.Getter;

import java.sql.Date;

/**
 * Holds the subset of PROC_GLOB_DOC_MASTER_VAL_LOAD output needed
 * for gateway-level period validation (mirrors legacy LoadDocumentValuesFromDB).
 *
 * Note: financial period (FINANCIAL_PERIOD_START/END) is NOT returned by the
 * procedure — it must be loaded separately from GLOBAL_COMPANY_MASTER.
 */
@Getter
@Builder
public class DocumentMasterInfo {

    private final String docType;

    /** true when GL_POSTING = 'Y' — financial and transaction period checks apply */
    private final boolean glDocument;

    /** true when INVENTORY_DOCUMENT = 'Y' — stock period check applies */
    private final boolean inventoryDocument;

    // Transaction period returned by the procedure
    private final Date transPeriodStart;
    private final Date transPeriodEnd;

    // Stock / inventory period returned by the procedure
    private final Date stockPeriodStart;
    private final Date stockPeriodEnd;
}
