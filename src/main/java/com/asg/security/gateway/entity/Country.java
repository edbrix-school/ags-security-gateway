package com.asg.security.gateway.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "GLOBAL_COUNTRY_MASTER")
public class Country {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "COUNTRY_POID")
    private Long countryPoid;

    @Column(name = "GROUP_POID")
    private Long groupPoid;

    @Column(name = "COUNTRY_CODE")
    private String countryCode;

    @Column(name = "COUNTRY_NAME")
    private String countryName;

    @Column(name = "COUNTRY_NAME2")
    private String countryName2;

    @Column(name = "REGION_POID")
    private Long regionPoid;

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

    @Column(name = "MANIFEST_COUNTRY_REMARK")
    private String manifestCountryRemark;

//    @Column(name = "OFAC_BAN")
//    private Boolean ofacBan;
//
//    @Column(name = "TRADE_BAN")
//    private Boolean tradeBan;

    @Column(name = "COUNTRY_TICKET_RATE")
    private Double countryTicketRate;
}
