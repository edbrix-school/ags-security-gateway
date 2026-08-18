package com.asg.security.gateway.repository;

import com.asg.security.gateway.dto.DocumentMasterInfo;
import com.asg.security.gateway.util.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

/**
 * Calls PROC_GLOB_DOC_MASTER_VAL_LOAD — the same procedure as the legacy
 * LoadDocumentValuesFromDB — to retrieve document flags and period dates
 * in a single round-trip.
 *
 * Returns: DOC_TYPE, GL_POSTING, INVENTORY_DOCUMENT,
 *          TRANS_PERIOD_START/END, STOCK_PERIOD_START/END.
 *
 * Financial period (FINANCIAL_PERIOD_START/END) is NOT returned by the
 * procedure — load it from GLOBAL_COMPANY_MASTER via CompanyRepository.
 */
@Repository
@Slf4j
@RequiredArgsConstructor
public class DocumentMasterInfoRepository {

    private final DataSource dataSource;

    /**
     * Loads document master info for the given docId using the current user's
     * group and company context.
     *
     * @return populated {@link DocumentMasterInfo}, or {@code null} if not found
     */
    public DocumentMasterInfo loadDocumentInfo(String docId) {
        String sql = "{CALL PROC_GLOB_DOC_MASTER_VAL_LOAD(?,?,?,?)}";

        try (Connection conn = dataSource.getConnection()) {
            // Postgres refcursors only live for the duration of the transaction that opened them
            conn.setAutoCommit(false);

            try (CallableStatement stmt = conn.prepareCall(sql)) {
                stmt.setLong(1, UserContext.getGroupPoid());
                stmt.setLong(2, UserContext.getCompanyPoid());
                stmt.setString(3, docId);
                stmt.registerOutParameter(4, Types.OTHER); // REF_CURSOR

                stmt.execute();

                try (ResultSet rs = (ResultSet) stmt.getObject(4)) {
                    if (rs != null && rs.next()) {
                        DocumentMasterInfo info = DocumentMasterInfo.builder()
                                .docType(rs.getString("DOC_TYPE"))
                                .glDocument("Y".equalsIgnoreCase(rs.getString("GL_POSTING")))
                                .inventoryDocument("Y".equalsIgnoreCase(rs.getString("INVENTORY_DOCUMENT")))
                                .transPeriodStart(rs.getDate("TRANS_PERIOD_START"))
                                .transPeriodEnd(rs.getDate("TRANS_PERIOD_END"))
                                .stockPeriodStart(rs.getDate("STOCK_PERIOD_START"))
                                .stockPeriodEnd(rs.getDate("STOCK_PERIOD_END"))
                                .build();
                        conn.commit();
                        return info;
                    }
                }
            }
            conn.commit();

        } catch (SQLException e) {
            log.error("Error calling PROC_GLOB_DOC_MASTER_VAL_LOAD for docId={}: {}", docId, e.getMessage(), e);
        }

        return null;
    }
}
