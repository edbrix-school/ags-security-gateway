package com.asg.security.gateway.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "GLOBAL_STATE_MASTER")
public class State {

    @Id
    @Column(name = "STATE_POID")
    private Long statePoid;

    @Column(name = "GROUP_POID")
    private Long groupPoid;

    @Column(name = "STATE_NAME")
    private String stateName;

    @Column(name = "COUNTRY_POID")
    private Long countryPoid;

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

    @Column(name = "DELETED")
    private String deleted;

    @Column(name = "STATE_REMARK")
    private String stateRemark;

}
