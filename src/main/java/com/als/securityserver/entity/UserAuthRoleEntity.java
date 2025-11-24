package com.als.securityserver.entity;

import com.als.securityserver.entity.key.UserRolesEntityKey;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;

import java.sql.Date;
import java.time.LocalDateTime;


@Data
@Entity
@Table(name = "GLOBAL_USERS_AUTH_ROLES_DTL")
public class UserAuthRoleEntity {

    @EmbeddedId
    private UserRolesEntityKey id;

    @Column(name = "USER_ROLE_POID")
    private Long userRolePoid;

    @Column(name = "EXPIRY_DATE")
    private Date expiryDate;

    @Column(name = "CREATED_BY")
    private String createdBy;

    @Column(name = "CREATED_DATE")
    private LocalDateTime createdDate;

    @Column(name = "LASTMODIFIED_BY")
    private String lastModifiedBy;

    @Column(name = "LASTMODIFIED_DATE")
    private LocalDateTime lastModifiedDate;
}