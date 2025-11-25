package com.asg.security.gateway.model;

import java.io.Serializable;

public class AuthenticationDetails implements Serializable {

    private static final long serialVersionUID = 550269061132507976L;

    private Long loggedInUserPoid;
    private String loggedInUserId;
    private String loggedInUserName;
    private String loggedInUserEmail;
    private String loggedInUserRole;
    private Long groupPoid;
    private Long companyPoid;

    public Long getLoggedInUserPoid() {
        return loggedInUserPoid;
    }

    public void setLoggedInUserPoid(Long loggedInUserPoid) {
        this.loggedInUserPoid = loggedInUserPoid;
    }

    public String getLoggedInUserId() {
        return loggedInUserId;
    }

    public void setLoggedInUserId(String loggedInUserId) {
        this.loggedInUserId = loggedInUserId;
    }

    public String getLoggedInUserName() {
        return loggedInUserName;
    }

    public void setLoggedInUserName(String loggedInUserName) {
        this.loggedInUserName = loggedInUserName;
    }

    public String getLoggedInUserEmail() {
        return loggedInUserEmail;
    }

    public void setLoggedInUserEmail(String loggedInUserEmail) {
        this.loggedInUserEmail = loggedInUserEmail;
    }

    public String getLoggedInUserRole() {
        return loggedInUserRole;
    }

    public void setLoggedInUserRole(String loggedInUserRole) {
        this.loggedInUserRole = loggedInUserRole;
    }

    public Long getGroupPoid() {
        return groupPoid;
    }

    public void setGroupPoid(Long groupPoid) {
        this.groupPoid = groupPoid;
    }

    public Long getCompanyPoid() {
        return companyPoid;
    }

    public void setCompanyPoid(Long companyPoid) {
        this.companyPoid = companyPoid;
    }
}

