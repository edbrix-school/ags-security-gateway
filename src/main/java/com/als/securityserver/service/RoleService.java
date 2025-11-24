package com.als.securityserver.service;

import com.als.securityserver.entity.UserAuthRoleEntity;
import com.als.securityserver.entity.UserRolesEntity;
import com.als.securityserver.entity.master.RoleEntity;
import com.als.securityserver.repository.RoleRepository;
import com.als.securityserver.repository.UserAuthRoleRepository;
import com.als.securityserver.repository.UserRoleRepository;
import org.springframework.stereotype.Service;
import com.alsharif.securityserver.dto.UserAuthRoleDto;
import com.alsharif.securityserver.dto.UserRoleDto;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
@Service
public class RoleService {
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final UserAuthRoleRepository userAuthRoleRepository;

    public RoleService(RoleRepository roleRepository,
                       UserRoleRepository userRoleRepository,
                       UserAuthRoleRepository userAuthRoleRepository) {
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
        this.userAuthRoleRepository = userAuthRoleRepository;
    }

    /**
     * Retrieves the complete role records mapped to the given user.
     * Mirrors the legacy monolith behaviour, including the derived flags.
     */
    @Transactional(readOnly = true)
    public List<UserRoleDto> getUserRoles(Long userPoid) {
        try {
            return userRoleRepository.findAllById_UserPoid(userPoid).stream()
                    .map(this::mapToDto)
                    .filter(Objects::nonNull)
                    .toList();
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to resolve user roles for user " + userPoid, ex);
        }
    }

    private UserRoleDto mapToDto(UserRolesEntity userRole) {
        RoleEntity role = roleRepository.findByUserRolePoid(userRole.getUserRolePoid());
        if (role == null) {
            return null;
        }
        return new UserRoleDto(
                role.getUserRolePoid(),
                role.getUserRoleId(),
                role.getUserRoleName(),
                userRole.getExpiryDate(),
                role.getDeleted(),
                role.getActive(),
                ""
        );
    }

    /**
     * Derived helper mirroring the monolith helper to surface only the role names.
     */
    @Transactional(readOnly = true)
    public List<String> getUserRoleNames(Long userPoid) {
        return getUserRoles(userPoid).stream()
                .map(UserRoleDto::roleName)
                .toList();
    }

    /**
     * Replica of the monolith's user authorization role resolution.
     */
    @Transactional(readOnly = true)
    public List<UserAuthRoleDto> getUserAuthRoles(Long userPoid) {
        List<UserAuthRoleEntity> authRoles = userAuthRoleRepository.findAllById_UserPoid(userPoid);

        return authRoles.stream()
                .map(this::mapToAuthRoleDto)
                .filter(Objects::nonNull)
                .toList();
    }

    private UserAuthRoleDto mapToAuthRoleDto(UserAuthRoleEntity entity) {
        RoleEntity role = roleRepository.findByUserRolePoid(entity.getUserRolePoid());
        if (role == null) {
            return null;
        }
        UserAuthRoleDto dto = new UserAuthRoleDto();
        dto.setUserPoid(entity.getId().getUserPoid());
        dto.setDetRowId(entity.getId().getDetRowId());
        dto.setUserRolePoId(entity.getUserRolePoid());
        dto.setRoleId(role.getUserRoleId());
        dto.setRoleName(role.getUserRoleName());
        dto.setExpiryDate(entity.getExpiryDate());
        dto.setDeleted(StringUtils.isBlank(role.getDeleted()) ? "N" : role.getDeleted());
        dto.setActive(role.getActive());
        dto.setActionType("");
        return dto;
    }
}
