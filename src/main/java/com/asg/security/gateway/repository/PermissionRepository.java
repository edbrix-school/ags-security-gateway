package com.asg.security.gateway.repository;

import com.asg.security.gateway.dto.PermissionDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

@Repository
public class PermissionRepository {

    @Autowired
    private DataSource dataSource;

    private static final Logger log = LoggerFactory.getLogger(PermissionRepository.class);

    public List<PermissionDto> getUserPermissions(String userId) throws SQLException {
        List<PermissionDto> permissions = new ArrayList<>();

        try (Connection conn = dataSource.getConnection()) {
            // Postgres refcursors only live for the duration of the transaction that opened them
            conn.setAutoCommit(false);

            // PROC_GLOB_USR_RIGHTS_APPSTART's refcursor OUT param is second, not first — pgjdbc's
            // CallableStatement.registerOutParameter only supports REF_CURSOR in position one, so
            // it silently drops this one instead of binding it. Calling the procedure as a plain
            // CALL query sidesteps that limitation entirely: Postgres returns INOUT values (here,
            // the cursor's name) as a one-row ResultSet, which we then FETCH from separately.
            String cursorName;
            try (PreparedStatement ps = conn.prepareStatement("CALL PROC_GLOB_USR_RIGHTS_APPSTART(?, NULL)")) {
                ps.setString(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    cursorName = rs.getString(1);
                }
            }

            try (Statement fetchStmt = conn.createStatement();
                 ResultSet rs = fetchStmt.executeQuery("FETCH ALL FROM \"" + cursorName + "\"")) {
                while (rs.next()) {
                    String userPoid = rs.getString("USER_POID");
                    String docId    = rs.getString("DOC_ID");
                    String rights   = rs.getString("RIGHTS");

                    permissions.add(new PermissionDto(userPoid, docId, rights));
                }
            }
            conn.commit();
        }
        catch (SQLException e) {
            log.error("Error while calling stored procedure PROC_GLOB_USR_RIGHTS_APPSTART", e);
            throw new SQLException("Error while calling PROC_GLOB_USR_RIGHTS_APPSTART", e);
        }

//        log.debug("Stored procedure PROC_GLOB_USR_RIGHTS_APPSTART returned: {}", permissions);
        return permissions;
    }
}


