package com.asg.security.gateway.entity.key;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;

import java.io.Serializable;

@Embeddable
@Data
public class CompanyDivisionEntityKey implements Serializable {

    @Column(name = "COMPANY_POID")
    private Long companyPoid;

    @Column(name = "DET_ROW_ID")
    private Long detRowId;
}

