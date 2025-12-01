package com.asg.security.gateway.repository;

import com.asg.security.gateway.dto.PermissionDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class PermissionRepository {

    @Autowired
    private DataSource dataSource;

    private static final Logger log = LoggerFactory.getLogger(PermissionRepository.class);

    public List<PermissionDto> getUserPermissions(String userId) throws SQLException {
        String sql = "{ CALL PROC_GLOB_USR_RIGHTS_APPSTART(?, ?) }";

        List<PermissionDto> permissions = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             CallableStatement cs = conn.prepareCall(sql)) {

            cs.setString(1, userId);
            cs.registerOutParameter(2, oracle.jdbc.OracleTypes.CURSOR); // Oracle-specific

            cs.execute();

            try (ResultSet rs = (ResultSet) cs.getObject(2)) {
                while (rs.next()) {
                    String userPoid = rs.getString("USER_POID");
                    String docId    = rs.getString("DOC_ID");
                    String rights   = rs.getString("RIGHTS");

                    permissions.add(new PermissionDto(userPoid, docId, rights));
                }
            }
        }
        catch (SQLException e) {
            log.error("Error while calling stored procedure PROC_GLOB_USR_RIGHTS_APPSTART", e);
            throw new SQLException("Error while calling PROC_GLOB_USR_RIGHTS_APPSTART", e);
        }

//        log.debug("Stored procedure PROC_GLOB_USR_RIGHTS_APPSTART returned: {}", permissions);
        return permissions;
    }
}


