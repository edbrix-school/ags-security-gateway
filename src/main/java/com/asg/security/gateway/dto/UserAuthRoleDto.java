package com.alsharif.securityserver.dto;

import lombok.Data;

import java.io.Serializable;
import java.sql.Date;

@Data
public class UserAuthRoleDto implements Serializable {

    private Long userPoid;
    private Long detRowId;
    private Date expiryDate;
    private Long userRolePoId;
    private String roleId;
    private String roleName;
    private String deleted;
    private String active;
    private String actionType;
}


