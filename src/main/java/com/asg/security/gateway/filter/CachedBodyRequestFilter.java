package com.asg.security.gateway.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Order(1)
public class CachedBodyRequestFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (request instanceof HttpServletRequest httpRequest) {
            String contentType = httpRequest.getContentType();
            // Cache the body for multipart requests before Spring's multipart resolver consumes it
            if (contentType != null && contentType.toLowerCase().startsWith("multipart/")) {
                CachedBodyHttpServletRequest cachedBodyRequest = new CachedBodyHttpServletRequest(httpRequest);
                chain.doFilter(cachedBodyRequest, response);
                return;
            }
        }
        chain.doFilter(request, response);
    }
}

