package com.asg.security.gateway.entity;

import com.asg.security.gateway.entity.key.CompanyDivisionEntityKey;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Base64;

@Entity
@Table(name = "GLOBAL_COMPANY_MASTER_DIV_DTL")
@Data
public class CompanyDivisionEntity {

    @EmbeddedId
    private CompanyDivisionEntityKey id;

    @Column(name = "DIV_POID")
    private Long divPoid;

    @Column(name = "REMARKS")
    private String remarks;

    @Column(name = "CREATED_BY")
    private String createdBy;

    @Column(name = "CREATED_DATE")
    private LocalDateTime createdDate;

    @Column(name = "LASTMODIFIED_BY")
    private String lastModifiedBy;

    @Column(name = "LASTMODIFIED_DATE")
    private LocalDateTime lastModifiedDate;

    @Lob
    @Column(name = "COMPANY_DIV_LOGO")
    private byte[] companyDivLogo;

    @Transient
    public String getLogoImageBase64() {
        if (this.companyDivLogo != null) {
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(this.companyDivLogo);
        }
        return null;
    }

    @Transient
    public void setLogoImageBase64(String base64Image) {
        if (base64Image != null && !base64Image.isBlank()) {
            // remove prefix if present
            String cleanBase64 = base64Image.replaceFirst("^data:image/[^;]+;base64,", "");
            this.companyDivLogo = Base64.getDecoder().decode(cleanBase64);
        } else {
            this.companyDivLogo = null;
        }
    }

    @Column(name = "COMPANY_DIV_ADDRESS")
    private String companyDivAddress;

    @Column(name = "DIVISION_NAME")
    private String divisionName;

    @Column(name = "COMPANY_DIV_ADDRESS_POS")
    private String companyDivAddressPos;

    @Transient
    public String actionType;
}

