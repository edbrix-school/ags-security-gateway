package com.asg.security.gateway.entity;

import com.asg.security.gateway.dto.TimeZoneDto;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Base64;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "GLOBAL_COMPANY_MASTER")
@Data
public class Company {

    @Column(name = "GROUP_POID")
    private Long groupPoid;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "COMPANY_POID")
    private Long companyPoid;

    @Column(name = "COMPANY_CODE", length = 20)
    private String companyCode;

    @Column(name = "COMPANY_NAME", length = 200)
    private String companyName;

    @Transient  //doesnt map to field in db
    private String label;

    @Transient  //doesnt map to field in db
    private Long value;

    @Transient  //doesnt map to field in db
    private TimeZoneDto timeZone;

    @Column(name = "COMPANY_NAME2", length = 200)
    private String companyName2;

    @NotBlank(message = "Contact Person is required")
    @Column(name = "CONTACT_PERSON")
    private String contactPerson;

    @Column(name = "TELEPHONE")
    private String telephone;

    @Column(name = "FAX")
    private String fax;

    @NotBlank
//    @Pattern(regexp = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$", message = "Email is not in proper format")
    @Email(message = "Email is not in proper format")
    @Column(name = "EMAIL")
    private String email;

    @Column(name = "COUNTRY")
    private String countryId;

    @Column(name = "ADDRESS")
    private String address;

    @NotNull(message = "Financial Period Start is required")
    @Column(name = "FINANCIAL_PERIOD_START")
    private Date financialPeriodStart;

    @NotNull(message = "Financial Period End is required")
    @Column(name = "FINANCIAL_PERION_END")
    private Date financialPeriodEnd;

    @NotNull(message = "Report Period Start is required")
    @Column(name = "REPORT_PERIOD_START")
    private Date reportPeriodStart;

    @NotNull(message = "Report Period End is required")
    @Column(name = "REPORT_PERIOD_END")
    private Date reportPeriodEnd;

    @NotNull(message = "Trans Period Start is required")
    @Column(name = "TRANS_PERIOD_START")
    private Date transPeriodStart;

    @NotNull(message = "Trans Period End is required")
    @Column(name = "TRANS_PERIOD_END")
    private Date transPeriodEnd;

    @Column(name = "ACTIVE")
    private String active;

    @Column(name = "SEQNO")
    private Integer seqNo;

    @Column(name = "CREATED_BY")
    private String createdBy;

    @Column(name = "CREATED_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdDate;

    @Column(name = "LASTMODIFIED_BY")
    private String lastModifiedBy;

    @Column(name = "LASTMODIFIED_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastModifiedDate;

    @Column(name = "DELETED")
    private String deleted;

//    @Lob
//    @Column(name = "LOGO_HDR_IMAGE")
//    private Byte[] logoHdrImage;

    @Column(name = "PROVISIONAL_CLOSED_DATE")
    private Date provisionalClosedDate;

    @Column(name = "BANK_DETAIL")
    private String bankDetail;

    @Column(name = "BANK_POID")
    private Long bankPoid;

    @Column(name = "TIN_NUMBER")
    private String tinNumber;

    @Column(name = "VAT_REGISTRATION_DATE")
    private Date vatRegistrationDate;

    @Column(name = "VAT_LAST_FILED_DATE")
    private Date vatLastFiledDate;

    @Column(name = "ACCOUNT_PERSON")
    private String accountPerson;

    @Column(name = "STOCK_PERIOD_START")
    private Date stockPeriodStart;  //inv_period_start

    @Column(name = "STOCK_PERIOD_END")
    private Date stockPeriodEnd;    //inv_period_end

    @Column(name = "VAT_FILING_PERIOD")
    private String vatFilingPeriod;

    @Column(name = "ACCOUNT_EMAIL")
    private String accountEmail;

    @Column(name = "VAT_LAST_FILED_BY")
    private String vatLastFiledBy;

    @Column(name = "VAT_LAST_FILED_CREATED_DATE")
    private Date vatLastFiledCreatedDate;

    @Column(name = "FINANCIAL_DATE_UPDATED_BY")
    private String financialDateUpdatedBy;

    @Column(name = "FINANCIAL_DATE_UPDATED_DATE")
    private Date financialDateUpdatedDate;

    @Column(name = "TRANS_DATE_UPDATED_BY")
    private String transDateUpdatedBy;

    @Column(name = "TRANS_DATE_UPDATED_DATE")
    private Date transDateUpdatedDate;

    @Column(name = "REPORT_DATE_UPDATED_BY")
    private String reportDateUpdatedBy;

    @Column(name = "REPORT_DATE_UPDATED_DATE")
    private Date reportDateUpdatedDate;

    @Column(name = "INVENTORY_DATE_UPDATED_BY")
    private String inventoryDateUpdatedBy;

    @Column(name = "INVENTORY_DATE_UPDATED_DATE")
    private Date inventoryDateUpdatedDate;

    @Transient  //doesnt map to field in db
    private String countryCode;

    @Transient  //doesnt map to field in db
    private String stateName;

    @Lob
    @Column(name = "LOGO_IMAGE")
    private byte[] logoImage;

    @Transient
    public String getLogoImageBase64() {
        if (this.logoImage != null) {
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(this.logoImage);
        }
        return null;
    }

    @Transient
    public void setLogoImageBase64(String base64Image) {
        if (base64Image != null && !base64Image.isBlank()) {
            // remove prefix if present
            String cleanBase64 = base64Image.replaceFirst("^data:image/[^;]+;base64,", "");
            this.logoImage = Base64.getDecoder().decode(cleanBase64);
        } else {
            this.logoImage = null;
        }
    }

    @Column(name = "DATE_FORMAT")
    private String dateFormat;

    @Column(name = "TIMEZONE_ID")
    private Long timezoneId;

    @Column(name = "CURRENCY_POID")
    private Long currencyPoid;

    @Transient
    private List<CompanyDivisionEntity> divisions;

    @Column(name = "STATE")
    private String stateId;

    @Column(name = "COLOR")
    private String companyColor;

    @Column(name = "SUBMISSION_PERIOD")
    private Long submissionPeriod;

}
