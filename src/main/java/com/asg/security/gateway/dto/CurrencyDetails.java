package com.asg.security.gateway.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CurrencyDetails {
    private Long currencyPoid;
    private String currencyCode;
    private String currencyName;
    private String currencyName2;
    private String coinShortName;
    private String currencyShortName;
    private String active;
    private Integer decimals;
}
