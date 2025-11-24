package com.als.securityserver.entity.key;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;

import java.io.Serializable;

@Embeddable
@Data
public class UserRolesEntityKey implements Serializable {

    @Column(name = "USER_POID")
    private Long userPoid;

    @Column(name = "DET_ROW_ID")
    private Long detRowId;
}

