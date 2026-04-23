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
                        "/asg/finance/api/v1/billwise-breakup/**",
                        "/asg/finance/api/v1/common/**"
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
