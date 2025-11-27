package com.asg.security.gateway.controller;

import com.asg.security.gateway.service.RoutingService;
import com.asg.security.gateway.util.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
public class ProxyController {

    private final RoutingService routingService;

    public ProxyController(RoutingService routingService) {
        this.routingService = routingService;
    }

    @RequestMapping("/api/{service}/**")
    public ResponseEntity<?> proxy(@PathVariable("service") String service,
                                   HttpServletRequest request,
                                   @RequestBody(required = false) byte[] body) throws IOException {
        byte[] requestBody = body != null ? body : StreamUtils.copyToByteArray(request.getInputStream());

        try {
            ResponseEntity<byte[]> response = routingService.forward(service, request, requestBody);
            HttpHeaders headers = new HttpHeaders();
            response.getHeaders().forEach((key, values) -> headers.put(key, List.copyOf(values)));
            return new ResponseEntity<>(response.getBody(), headers, response.getStatusCode());
        } catch (IllegalArgumentException ex) {
            return ApiResponse.error(ex.getMessage(), 502);
        }
    }
}

