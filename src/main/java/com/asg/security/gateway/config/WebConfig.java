package com.asg.security.gateway.config;

import com.asg.security.gateway.filter.FinancialDateValidationInterceptor;
import com.asg.security.gateway.filter.RBACInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private RBACInterceptor rbacInterceptor;

    @Autowired
    private FinancialDateValidationInterceptor financialDateValidationInterceptor;

    @Value("#{'${rbac.validate.services}'.split(',')}")
    private List<String> rbacServicesToValidate;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(rbacInterceptor)
                .addPathPatterns(rbacServicesToValidate)
                .excludePathPatterns(
                        "/asg/finance/api/ws",
                        "/asg/finance/api/ws/**",
                        "/asg/finance/api/websocket",
                        "/asg/finance/api/websocket/**",
                        "/asg/settings/api/v1/task-category/subcategories/**",
                        "/asg/settings/api/v1/document/details",
                        "/asg/settings/api/v1/document/searchable-fields/**",
                        "/asg/settings/api/v1/document/*/grant-edit-permission/status",
                        "/asg/settings/api/v1/document/*/grant-edit-permission",
                        "/asg/finance/api/v1/billwise-breakup/**",
                        "/asg/finance/api/v1/common/**",
                        // Shared ISO LOVs: called from Risk & Opportunities, CAR, Quality
                        // Objectives, ... so each caller sends its own X-Document-Id. A
                        // docid.api.mappings entry would pin them to one screen's doc id and
                        // reject the rest; the endpoints scope their own data to the logged-in PIC.
                        "/asg/admin-iso/api/v1/iso-lov/**",
                        // Shared HR endpoints, same reason: current-employee is called from leave
                        // request, resignation, personal data sheet, ... each with its own doc id.
                        // The endpoint reads the caller's rights itself through
                        // PROC_GLOB_USR_RIGHTS_APPSTART and gives a user without Edit on the screen
                        // they are on nothing but their own employee.
                        "/asg/hr/api/v1/common/**"
                );

        // Runs on ALL APIs — reads transactionDate from JSON body and validates
        // against company period. Self-skips when body has no transactionDate field.
        registry.addInterceptor(financialDateValidationInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/api/v1/auth/**",
                        "/actuator/**",
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/asg/finance/api/ws",
                        "/asg/finance/api/ws/**",
                        "/asg/finance/api/websocket",
                        "/asg/finance/api/websocket/**"
                );
    }
}
