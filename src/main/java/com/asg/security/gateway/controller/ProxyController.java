package com.asg.security.gateway.controller;

import com.asg.security.gateway.filter.CachedBodyHttpServletRequest;
import com.asg.security.gateway.service.RoutingService;
import com.asg.security.gateway.util.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@RestController
public class ProxyController {

    // Hop-by-hop headers (RFC 7230 §6.1) plus Content-Length: these describe the
    // downstream connection, not the payload, and must not be copied to the client.
    // The response body is fully buffered, so the container recomputes the correct
    // Content-Length / Transfer-Encoding. Copying them verbatim produces duplicate
    // Transfer-Encoding headers, which nginx rejects with a 502.
    private static final Set<String> HOP_BY_HOP_HEADERS = Set.of(
            "connection", "keep-alive", "proxy-authenticate", "proxy-authorization",
            "te", "trailer", "transfer-encoding", "upgrade", "content-length");

    private final RoutingService routingService;
    private final ObjectMapper objectMapper;

    public ProxyController(RoutingService routingService) {
        this.routingService = routingService;
        this.objectMapper = new ObjectMapper();
    }

    @RequestMapping(value = "/asg/{service}/api/**", headers = "Upgrade!=websocket")
    public ResponseEntity<?> proxy(@PathVariable("service") String service,
                                   HttpServletRequest request,
                                   @RequestBody(required = false) byte[] body) throws IOException {
        byte[] requestBody;
        
        // If request is a cached body request (multipart), use the cached body
        if (request instanceof CachedBodyHttpServletRequest cachedRequest) {
            requestBody = cachedRequest.getCachedBody();
        } else if (body != null) {
            requestBody = body;
        } else {
            requestBody = StreamUtils.copyToByteArray(request.getInputStream());
        }

        try {
            ResponseEntity<byte[]> response = routingService.forward(service, request, requestBody);
            HttpHeaders headers = new HttpHeaders();
            response.getHeaders().forEach((key, values) -> {
                if (!HOP_BY_HOP_HEADERS.contains(key.toLowerCase(Locale.ROOT))) {
                    headers.put(key, List.copyOf(values));
                }
            });
            return new ResponseEntity<>(response.getBody(), headers, response.getStatusCode());
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            // Parse the downstream service's response body as JSON and return it as-is
            String responseBody = ex.getResponseBodyAsString();
            Object responseObject;
            
            if (responseBody != null && !responseBody.trim().isEmpty()) {
                try {
                    // Parse JSON response from downstream service
                    responseObject = objectMapper.readValue(responseBody, Object.class);
                } catch (Exception parseEx) {
                    // If parsing fails, return the raw string wrapped in a map
                    responseObject = Map.of(
                        "success", false,
                        "statusCode", ex.getStatusCode().value(),
                        "message", responseBody
                    );
                }
            } else {
                // Empty response body
                responseObject = Map.of(
                    "success", false,
                    "statusCode", ex.getStatusCode().value(),
                    "message", "No response body from downstream service"
                );
            }
            
            return ResponseEntity.status(ex.getStatusCode()).body(responseObject);
        } catch (IllegalArgumentException ex) {
            return ApiResponse.error(ex.getMessage(), 502);
        } catch (RestClientException ex) {
            // Handle other RestClient exceptions (connection errors, timeouts, etc.)
            return ApiResponse.error("Service unavailable: " + ex.getMessage(), 503);
        }
    }
}
