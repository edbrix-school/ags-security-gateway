package com.als.securityserver.aad;

import com.als.securityserver.exception.AzureAuthenticationException;
import com.als.securityserver.model.AzureADResponse;
import com.als.securityserver.model.UserDetail;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Component
public class AzureADClient {

    private static final Logger logger = LoggerFactory.getLogger(AzureADClient.class);

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final JSONParser jsonParser;

    @Value("${aad.graph-api-endpoint}")
    private String graphApiEndpoint;

    public AzureADClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(java.time.Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper();
        this.jsonParser = new JSONParser(JSONParser.MODE_PERMISSIVE);
    }

    public AzureADResponse validate(String userEmail, String accessToken) {
        if (StringUtils.isBlank(accessToken)) {
            throw new AzureAuthenticationException("Access Token Not Found");
        }

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(graphApiEndpoint))
                    .header("Authorization", "Bearer " + accessToken)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            AzureADResponse aadResponse = new AzureADResponse();

            if (response.statusCode() == HttpStatus.OK.value()) {
                UserDetail userDetail = objectMapper.readValue(response.body(), UserDetail.class);
                logger.info("Azure response userPrincipalName={}, mail={}", userDetail.getUserPrincipalName(), userDetail.getMail());

                aadResponse.setStatusCode(response.statusCode());
                aadResponse.setStatusMessage("Access token is Valid. Successfully retrieved user details");

                if (matchesUserEmail(userEmail, userDetail)) {
                    aadResponse.setUserDetail(userDetail);
                    return aadResponse;
                }
                throw new AzureAuthenticationException("Email not found in Azure directory", aadResponse);
            }

            JSONObject responseJson = (JSONObject) jsonParser.parse(response.body());
            JSONObject errorJson = (JSONObject) responseJson.get("error");
            String message = errorJson != null ? (String) errorJson.get("message") : "Unknown error from Azure";
            String code = errorJson != null ? (String) errorJson.get("code") : null;
            aadResponse.setStatusCode(response.statusCode());
            aadResponse.setCode(code);
            aadResponse.setStatusMessage(message);
            throw new AzureAuthenticationException(message, aadResponse);

        } catch (AzureAuthenticationException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.error("Error occurred while retrieving user details for {}", userEmail, ex);
            throw new AzureAuthenticationException("Azure validation failed: " + ex.getMessage());
        }
    }

    private boolean matchesUserEmail(String userEmail, UserDetail userDetail) {
        if (userDetail == null) {
            return false;
        }
        return StringUtils.equalsIgnoreCase(userEmail, userDetail.getMail())
                || StringUtils.equalsIgnoreCase(userEmail, userDetail.getUserPrincipalName());
    }
}

