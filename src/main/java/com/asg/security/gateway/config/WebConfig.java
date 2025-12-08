package com.asg.security.gateway.config;

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

    @Value("#{'${rbac.validate.services}'.split(',')}")
    private List<String> rbacServicesToValidate;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(rbacInterceptor)
                .addPathPatterns(rbacServicesToValidate)
                .excludePathPatterns(
                        "/api/v1/auth/login",
                        "/api/v1/auth/send-otp",
                        "/api/v1/auth/forgot-password",
                        "/api/v1/auth/change-password",
                        "/api/v1/auth/refreshToken",
                        "/api/v1/auth/sso",
                        "/api/v1/auth/logout",
                        "/api/v1/users/menu",
                        "/api/v1/users/recent-menu",
                        "/api/v1/users/favorite-menu/**",
                        "/api/v1/users/permissions",
                        "/actuator/*",
                        // Excluding on asha request
                        "/api/v1/users/company",
                        "/api/v1/lovs/**",
                        "/api/v1/task-category/subcategories/**",
                        "/api/v1/attachments/**",
                        "/api/v1/draft/**",
                        "/api/v1/document/details",
                        "/api/v1/billwise-breakup/**",
                        // Swagger-specific paths
                        "/swagger-ui.html",
                        "/swagger-ui/**",
                        "/v3/api-docs",
                        "/v3/api-docs/**",
                        "/v3/api-docs.yaml",
                        "/swagger-resources/**",
                        "/webjars/**",
                        "/api/v1/dashboard/**",
                        "/api/v1/documentLock/**",
                        "/api/v1/common/**",
                        "/api/v1/user-preference/**",
                        "/asg/common-services/api/**"
                );
    }
}
