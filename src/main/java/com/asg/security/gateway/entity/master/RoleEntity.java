package com.asg.security.gateway.entity.master;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "GLOBAL_USER_ROLES")
public class RoleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_role_seq")
    @SequenceGenerator(name = "user_role_seq", sequenceName = "GLOBAL_USER_ROLES_SEQ", allocationSize = 1)
    @Column(name = "USER_ROLE_POID")
    private Long userRolePoid;

    @Column(name = "GROUP_POID")
    private Long groupPoid;

    @Column(name = "USER_ROLE_ID")
    private String userRoleId;

    @Column(name = "USER_ROLE_NAME")
    private String userRoleName;

    @Column(name = "USER_ROLE_NAME2")
    private String userRoleName2;

    @Column(name = "ACTIVE")
    private String active;

    @Column(name = "SEQNO")
    private Integer seqNo;

    @Column(name = "CREATED_BY")
    private String createdBy;

    @Column(name = "CREATED_DATE")
    private LocalDateTime createdDate;

    @Column(name = "LASTMODIFIED_BY")
    private String lastModifiedBy;

    @Column(name = "LASTMODIFIED_DATE")
    private LocalDateTime lastModifiedDate;

    @Column(name = "COMPANY_POID")
    private Long companyPoid;

    @Column(name = "DELETED")
    private String deleted;
}



