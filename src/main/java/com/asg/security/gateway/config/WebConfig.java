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
                        "/api/v1/task-category/subcategories/**",
                        "/api/v1/document/details",
                        "/api/v1/document/searchable-fields/**"
                );
    }
}
