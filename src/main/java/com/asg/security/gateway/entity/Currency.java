package com.asg.security.gateway.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "GLOBAL_CURRENCY_MASTER")
@Data
public class Currency {
    
    @Id
    @Column(name = "CURRENCY_POID")
    private Long currencyPoid;
    
    @Column(name = "CURRENCY_CODE")
    private String currencyCode;
    
    @Column(name = "CURRENCY_NAME")
    private String currencyName;

    @Column(name = "CURRENCY_NAME2")
    private String currencyName2;
    
    @Column(name = "CURRENCY_SHORT_NAME")
    private String currencyShortName;

    @Column(name = "COIN_SHORT_NAME")
    private String coinShortName;
    
    @Column(name = "CURRENCY_DECIMALS")
    private Integer decimals;

    @Column(name = "ACTIVE", length = 1)
    private String active;
}
