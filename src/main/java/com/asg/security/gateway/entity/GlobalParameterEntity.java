package com.asg.security.gateway.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;


@Data
@Entity
@Table(name = "GLOBAL_PARAMETERS")
public class GlobalParameterEntity {

    @Id
    @Column(name = "PARAMETER_POID")
    private Long parameterPoid;

    @Column(name = "GROUP_POID")
    private Long groupPoid;

    @Column(name = "PARAMETER_NAME")
    private String parameterName;

    @Column(name = "PARAMETER_KEYID_TYPE")
    private String parameterKeyIdType;

    @Column(name = "PARAMETER_KEYID")
    private String parameterKeyId;

    @Column(name = "PARAMETER_VALUE")
    private String parameterValue;

    @Column(name = "PARAMETER_DETAILS")
    private String parameterDetails;

    @Column(name = "CATEGORY")
    private String category;

    @Column(name = "PARAMETER_LINUX_VALUE")
    private String parameterLinuxValue;

    @Column(name = "PARAMETER_TYPE")
    private String parameterType;
}
