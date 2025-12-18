package com.asg.security.gateway.entity;

import jakarta.persistence.Embeddable;
import lombok.Data;

import java.io.Serializable;

@Embeddable
@Data
public class CompanyEntityKey implements Serializable {

    private Long userPoid;
    private Long detRowId;
    private Long companyPoid;

}

