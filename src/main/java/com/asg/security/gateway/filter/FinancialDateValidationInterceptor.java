package com.asg.security.gateway.filter;

import com.asg.security.gateway.dto.DocumentMasterInfo;
import com.asg.security.gateway.entity.Company;
import com.asg.security.gateway.enums.UserRolesRightsEnum;
import com.asg.security.gateway.repository.CompanyRepository;
import com.asg.security.gateway.repository.DocumentMasterInfoRepository;
import com.asg.security.gateway.util.UserContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.sql.Date;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.Set;

/**
 * Validates the transaction date from the JSON request body against the company's
 * configured period limits before the request reaches any downstream service.
 *
 * The date is read from the {@code transactionDate} field in the JSON payload
 * (e.g. {@code "transactionDate": "2026-03-23"}). The body can be re-read by
 * downstream handlers because {@link CachedBodyRequestFilter} wraps JSON requests
 * in a {@link CachedBodyHttpServletRequest}.
 *
 * Document flags (GL, Inventory) and trans/stock period dates are loaded via
 * {@link DocumentMasterInfoRepository} (PROC_GLOB_DOC_MASTER_VAL_LOAD).
 * Financial period dates are loaded from GLOBAL_COMPANY_MASTER via {@link CompanyRepository}.
 *
 * Rules mirroring the legacy DocumentTemplateBean:
 *
 *  ACTION gate  — only CREATE, EDIT, DELETE are checked.
 *                 VIEW, PRINT, EMAIL always pass through.
 *
 *  DOC_TYPE gate — Masters documents are never period-restricted.
 *
 *  GL documents (GL_POSTING = Y):
 *    • Financial period  — STRICT, no override.  Outside → 403.
 *    • Transaction period — SOFT at gateway (override via PROC_GLOBAL_DOC_EDIT_RIGHT_GET
 *                           is handled downstream in common-services). Outside → 403 here
 *                           but service may still permit via temporary edit rights.
 *
 *  Inventory documents (INVENTORY_DOCUMENT = Y):
 *    • Stock period — same soft pattern as transaction period above.
 *
 *  Non-GL, Non-Inventory Transactions:
 *    • Legacy only shows a WARNING (not a hard block) for transaction period.
 *    • Gateway passes these through — the downstream save handles the warning.
 *
 *  No {@code transactionDate} in body → always passes (master data, non-dated endpoints).
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class FinancialDateValidationInterceptor implements HandlerInterceptor {

    static final String TRANSACTION_DATE_FIELD = "transactionDate";
    static final String ACTION_HEADER          = "X-Action-Requested";

    private static final String DOC_TYPE_TRANSACTIONS = "Transactions";

    private static final Set<String> DATE_VALIDATED_ACTIONS = Set.of(
            UserRolesRightsEnum.CREATE.name(),
            UserRolesRightsEnum.EDIT.name(),
            UserRolesRightsEnum.DELETE.name()
    );

    private final DocumentMasterInfoRepository documentMasterInfoRepository;
    private final CompanyRepository companyRepository;
    private final ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        // 1. Action gate — VIEW / PRINT / EMAIL never need a date check
        String action = request.getHeader(ACTION_HEADER);
        if (action == null || !DATE_VALIDATED_ACTIONS.contains(action.toUpperCase())) {
            return true;
        }

        // 2. Extract transactionDate from the JSON request body
        LocalDate txDate = extractTransactionDate(request);
        if (txDate == null) {
            // No transactionDate in body — non-dated endpoint, skip
            return true;
        }

        // 3. Load document flags + trans/stock period dates from PROC_GLOB_DOC_MASTER_VAL_LOAD
        String documentId = request.getHeader("X-Document-Id");
        if (documentId == null || documentId.isBlank()) {
            return true; // RBAC interceptor already validated this; just skip here
        }

        DocumentMasterInfo info = documentMasterInfoRepository.loadDocumentInfo(documentId);
        if (info == null) {
            return true; // unknown doc — let downstream handle it
        }

        // Masters have no period restrictions
        if (!DOC_TYPE_TRANSACTIONS.equalsIgnoreCase(info.getDocType())) {
            log.debug("Skipping period check for Masters document: docId={}", documentId);
            return true;
        }

        // Non-GL, non-inventory Transactions: legacy only warns, never hard-blocks at this stage
        if (!info.isGlDocument() && !info.isInventoryDocument()) {
            log.debug("Non-GL/non-inventory transaction — period check skipped at gateway: docId={}", documentId);
            return true;
        }

        // 4. Financial period — loaded from GLOBAL_COMPANY_MASTER (not returned by the proc)
        //
        //    Legacy rule (IsThisDateWithinValidFinancialPeriod):
        //      CREATE  → all Transactions (no GLDocument gate)
        //      EDIT / DELETE → GL Transactions only
        boolean isCreate = UserRolesRightsEnum.CREATE.name().equalsIgnoreCase(action);
        boolean needsFinancialCheck = isCreate || info.isGlDocument();

        if (needsFinancialCheck) {
            Long companyPoid = UserContext.getCompanyPoid();
            if (companyPoid == null) {
                return writeError(response, HttpStatus.UNAUTHORIZED, "Company context is missing");
            }
            Company company = companyRepository.findByCompanyPoid(companyPoid);
            if (company == null) {
                return writeError(response, HttpStatus.BAD_REQUEST,
                        "Company not found for companyPoid: " + companyPoid);
            }

            // Transaction period: from PROC_GLOB_DOC_MASTER_VAL_LOAD — GL docs only
            if (info.isGlDocument()
                    && !isWithinPeriod(txDate, info.getTransPeriodStart(), info.getTransPeriodEnd())) {
                log.warn("Transaction period violation: docId={}, txDate={}", documentId, txDate);
                return writeError(response, HttpStatus.FORBIDDEN,
                        "Transaction date " + txDate + " is outside the transaction period ("
                                + toLocalDate(info.getTransPeriodStart()) + " to "
                                + toLocalDate(info.getTransPeriodEnd()) + "). "
                                + "Contact your administrator for temporary edit rights if this entry is valid.");
            }

            if (!isWithinPeriod(txDate, company.getFinancialPeriodStart(), company.getFinancialPeriodEnd())) {
                log.warn("Financial period violation: docId={}, companyPoid={}, txDate={}", documentId, companyPoid, txDate);
                return writeError(response, HttpStatus.FORBIDDEN,
                        "Transaction date " + txDate + " is outside the financial period ("
                        + toLocalDate(company.getFinancialPeriodStart()) + " to "
                        + toLocalDate(company.getFinancialPeriodEnd()) + ")");
            }

        }

        // 5. Inventory document — stock period (from proc)
        if (info.isInventoryDocument()) {
            if (!isWithinPeriod(txDate, info.getStockPeriodStart(), info.getStockPeriodEnd())) {
                log.warn("Stock period violation: docId={}, txDate={}", documentId, txDate);
                return writeError(response, HttpStatus.FORBIDDEN,
                        "Transaction date " + txDate + " is outside the inventory (stock) period ("
                        + toLocalDate(info.getStockPeriodStart()) + " to "
                        + toLocalDate(info.getStockPeriodEnd()) + "). "
                        + "Contact your administrator for temporary edit rights if this entry is valid.");
            }
        }

        log.debug("Period validation passed: docId={}, txDate={}", documentId, txDate);
        return true;
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    /**
     * Reads the cached request body and extracts the {@code transactionDate} field.
     * Returns {@code null} if the body is absent, not JSON, or the field is missing.
     * Logs a warning (but does NOT reject) if the value is present but unparseable.
     */
    private LocalDate extractTransactionDate(HttpServletRequest request) {
        if (!(request instanceof CachedBodyHttpServletRequest cached)) {
            return null;
        }

        byte[] body = cached.getCachedBody();
        if (body == null || body.length == 0) {
            return null;
        }

        try {
            JsonNode root = objectMapper.readTree(body);
            JsonNode dateNode = root.get(TRANSACTION_DATE_FIELD);
            if (dateNode == null || dateNode.isNull() || dateNode.asText().isBlank()) {
                return null;
            }
            return LocalDate.parse(dateNode.asText().trim());
        } catch (DateTimeParseException e) {
            log.warn("Unparseable transactionDate in request body: {}", e.getMessage());
            return null; // let downstream handle bad date values
        } catch (Exception e) {
            log.debug("Could not read transactionDate from body: {}", e.getMessage());
            return null;
        }
    }

    private boolean isWithinPeriod(LocalDate date, Date start, Date end) {
        if (start == null || end == null) return false;
        return !date.isBefore(start.toLocalDate()) && !date.isAfter(end.toLocalDate());
    }

    private boolean isWithinPeriod(LocalDate date, java.util.Date start, java.util.Date end) {
        if (start == null || end == null) return false;
        LocalDate s = toLocalDate(start);
        LocalDate e = toLocalDate(end);
        return !date.isBefore(s) && !date.isAfter(e);
    }

    private LocalDate toLocalDate(java.util.Date date) {
        return date == null ? null : date.toInstant().atZone(ZoneOffset.UTC).toLocalDate();
    }

    private LocalDate toLocalDate(Date sqlDate) {
        return sqlDate == null ? null : sqlDate.toLocalDate();
    }

    private boolean writeError(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType("application/json");
        objectMapper.writeValue(response.getWriter(), Map.of(
                "success", false,
                "statusCode", status.value(),
                "message", message
        ));
        return false;
    }
}
