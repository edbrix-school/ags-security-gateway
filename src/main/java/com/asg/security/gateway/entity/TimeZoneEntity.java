package com.asg.security.gateway.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "TIME_ZONE_MASTER")
public class TimeZoneEntity {

    @Id
    @Column(name = "TIMEZONE_ID")
    private Long timezoneId;

    @Column(name = "TIMEZONE_CODE")
    private String timezoneCode;

    @Column(name = "TIMEZONE_NAME")
    private String timezoneName;

    @Column(name = "STATUS")
    private String status;

    @Column(name = "CREATED_DATE")
    private LocalDateTime createdDate;

    // Getters and Setters
}
