package com.asg.security.gateway.dto;

import java.sql.Date;

public record UserRoleDto(Long userRolePoId, String roleId, String roleName, Date expiryDate,
                          String deleted, String active, String actionType) {
}


