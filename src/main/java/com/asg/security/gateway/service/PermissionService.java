package com.asg.security.gateway.service;

import com.asg.security.gateway.dto.PermissionDto;
import com.asg.security.gateway.enums.UserRolesRightsEnum;
import com.asg.security.gateway.repository.PermissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class PermissionService {

    @Autowired
    private PermissionRepository permissionRepository;

    // Get all user permissions
    public List<PermissionDto> getUserPermissions(String userId) throws SQLException {
        return permissionRepository.getUserPermissions(userId);
    }

    // For Interceptor
    // To check if the user has that specific permission for that specific document
    public boolean hasPermission(String userId, String documentId, String action) throws SQLException {
        List<PermissionDto> permissions = getUserPermissions(userId);

        return permissions.stream()
                .filter(p -> p.docId().equals(documentId))
                .anyMatch(p -> hasRight(p.rights(), action));
    }

    // To decode the 110111 from db and give set of permission strings
    // Example decode: "101010" → ["VIEW","EDIT","PRINT"]
    public static Set<UserRolesRightsEnum> decode(String rights) {
        Set<UserRolesRightsEnum> allowed = new HashSet<>();
        if (rights == null || rights.isEmpty()) return allowed;

        UserRolesRightsEnum[] values = UserRolesRightsEnum.values();
        for (int i = 0; i < rights.length() && i < values.length; i++) {
            if (rights.charAt(i) == '1') {
                allowed.add(values[i]);
            }
        }
        return allowed;
    }

    // Utility to check if string action exists in decoded rights
    public static boolean hasRight(String rights, String action) {
        return decode(rights).contains(UserRolesRightsEnum.valueOf(action.toUpperCase()));
    }

}