package com.als.securityserver.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.Date;

@Entity
@Table(name = "GLOBAL_USERS")
public class User {

    @Id
    @Column(name = "USER_POID")
    private Long userPoid;

    @Column(name = "USER_ID")
    private String userId;

    @Column(name = "USER_NAME")
    private String userName;

    @Column(name = "USER_EMAIL")
    private String email;

    @Column(name = "GROUP_POID")
    private Long groupPoid;

    @Column(name = "DEFAULT_COMPANY_POID")
    private Long defaultCompanyPoid;

    @Column(name = "DEFAULT_LOCATION_POID")
    private Long defaultLocationPoid;

    @Column(name = "PWD")
    private String pwd;

    @Column(name = "ACTIVE")
    private String active;

    @Column(name = "USER_LOCKED")
    private String userLocked;

    @Column(name = "USER_LOCKED_REASON")
    private String userLockedReason;

    @Column(name = "RESET_PWD_NEXT_LOGIN")
    private String resetPasswordNextLogin;

    @Column(name = "CREATED_DATE")
    private Date createdDate;

    @Column(name = "LASTMODIFIED_DATE")
    private Date lastModifiedDate;

    @Column(name = "LASTMODIFIED_BY")
    private String lastModifiedBy;

    @Column(name = "USER_MOBILE")
    private String userMobile;

    public Long getUserPoid() {
        return userPoid;
    }

    public void setUserPoid(Long userPoid) {
        this.userPoid = userPoid;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Long getGroupPoid() {
        return groupPoid;
    }

    public void setGroupPoid(Long groupPoid) {
        this.groupPoid = groupPoid;
    }

    public Long getDefaultCompanyPoid() {
        return defaultCompanyPoid;
    }

    public void setDefaultCompanyPoid(Long defaultCompanyPoid) {
        this.defaultCompanyPoid = defaultCompanyPoid;
    }

    public Long getDefaultLocationPoid() {
        return defaultLocationPoid;
    }

    public void setDefaultLocationPoid(Long defaultLocationPoid) {
        this.defaultLocationPoid = defaultLocationPoid;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public String getActive() {
        return active;
    }

    public void setActive(String active) {
        this.active = active;
    }

    public String getUserLocked() {
        return userLocked;
    }

    public void setUserLocked(String userLocked) {
        this.userLocked = userLocked;
    }

    public String getUserLockedReason() {
        return userLockedReason;
    }

    public void setUserLockedReason(String userLockedReason) {
        this.userLockedReason = userLockedReason;
    }

    public String getResetPasswordNextLogin() {
        return resetPasswordNextLogin;
    }

    public void setResetPasswordNextLogin(String resetPasswordNextLogin) {
        this.resetPasswordNextLogin = resetPasswordNextLogin;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Date getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(Date lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    public String getUserMobile() {
        return userMobile;
    }

    public void setUserMobile(String userMobile) {
        this.userMobile = userMobile;
    }
}

