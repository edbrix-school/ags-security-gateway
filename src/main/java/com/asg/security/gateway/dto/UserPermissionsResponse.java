package com.asg.security.gateway.dto;

import java.util.List;

public record UserPermissionsResponse(String userId, List<PermissionDto> permissions) {}