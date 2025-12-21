package com.asg.security.gateway.service;

import com.asg.security.gateway.util.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMapAdapter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class RoutingService {

    private final RestTemplate restTemplate;
    private final Environment environment;

    public RoutingService(RestTemplate restTemplate, Environment environment) {
        this.restTemplate = restTemplate;
        this.environment = environment;
    }

    public ResponseEntity<byte[]> forward(String service,
                                          HttpServletRequest request,
                                          byte[] body) {
        String baseUrl = resolveServiceUrl(service);
        if (StringUtils.isBlank(baseUrl)) {
            throw new IllegalArgumentException("No backend mapping found for service: " + service);
        }

        String forwardPath = extractForwardPath(request, service);
        URI uri = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .path(forwardPath)
                .query(request.getQueryString())
                .build(true)
                .toUri();

        HttpMethod httpMethod = HttpMethod.valueOf(request.getMethod());

        HttpHeaders headers = copyRequestHeaders(request);
        addExtraHeaders(headers);
        headers.remove(HttpHeaders.HOST);

        HttpEntity<byte[]> httpEntity = new HttpEntity<>(body != null && body.length > 0 ? body : null, headers);
        return restTemplate.exchange(uri, httpMethod, httpEntity, byte[].class);
    }

    public String resolveServiceUrl(String service) {
        return environment.getProperty(service + ".url");
    }

    private String extractForwardPath(HttpServletRequest request, String service) {
        String requestUri = request.getRequestURI();
        String prefix = "/asg/" + service + "/api";
        String remainder = requestUri.length() > prefix.length()
                ? requestUri.substring(prefix.length())
                : "";
        if (StringUtils.isBlank(remainder)) {
            return "/";
        }
        return remainder.startsWith("/") ? remainder : "/" + remainder;
    }

    private HttpHeaders copyRequestHeaders(HttpServletRequest request) {
        Map<String, List<String>> headers = new LinkedHashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames != null && headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            Enumeration<String> values = request.getHeaders(headerName);
            List<String> valueList = new ArrayList<>();
            while (values.hasMoreElements()) {
                valueList.add(values.nextElement());
            }
            headers.put(headerName, valueList);
        }
        return new HttpHeaders(new MultiValueMapAdapter<>(headers));
    }

    private void addExtraHeaders(HttpHeaders headers) {
        headers.add("X-User-Name", UserContext.getUserName());
        headers.add("X-User-Poid", UserContext.getUserPoid() != null ? UserContext.getUserPoid().toString() : null);
        headers.add("X-User-Id", UserContext.getUserId());
        headers.add("X-User-Email", UserContext.getUserEmail());
        headers.add("X-User-Role", UserContext.getUserRole());
        headers.add("X-Group-Poid", UserContext.getGroupPoid() != null ? UserContext.getGroupPoid().toString() : null);

        if (!headers.containsKey("X-Company-Poid") || StringUtils.isBlank(headers.getFirst("X-Company-Poid"))) {
            headers.add("X-Company-Poid", UserContext.getCompanyPoid() != null ? UserContext.getCompanyPoid().toString() : null);
        }
    }
}

