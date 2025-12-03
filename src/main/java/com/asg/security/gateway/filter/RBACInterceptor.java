package com.asg.security.gateway.filter;

import com.asg.security.gateway.config.utility.DocIdApiMappingProperties;
import com.asg.security.gateway.enums.UserRolesRightsEnum;
import com.asg.security.gateway.repository.GlobalParameterRepository;
import com.asg.security.gateway.service.PermissionService;
import com.asg.security.gateway.util.UserContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.support.StandardMultipartHttpServletRequest;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class RBACInterceptor implements HandlerInterceptor {

    private final PermissionService permissionService;
    private final GlobalParameterRepository globalParameterRepository;
    private final DocIdApiMappingProperties docIdApiMappingProperties;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        //File Size Check (only for file upload endpoints)
        if (handler instanceof HandlerMethod method) {
            boolean hasFile = Arrays.stream(method.getMethodParameters())
                    .anyMatch(p -> MultipartFile.class.isAssignableFrom(p.getParameterType()) ||
                            (p.getParameterType().isArray() &&
                                    MultipartFile.class.isAssignableFrom(p.getParameterType().getComponentType())));

            if (hasFile && request instanceof StandardMultipartHttpServletRequest multiReq) {
                Integer limitMb = globalParameterRepository.findParameterValueByParameterName("SYSTEM", "ATTACHMENT_MAX_SIZE_MB");
                long maxSize = (limitMb == null ? 10 : limitMb) * 1024L * 1024L;

                for (MultipartFile file : multiReq.getFileMap().values()) {
                    if (file.getSize() > maxSize) {
                        writeErrorResponse(response, HttpStatus.BAD_REQUEST, "File exceeds " + (maxSize / 1024 / 1024) + " MB limit in db for file upload.");
                        return false;
                    }
                }
            }
        }

        //Action Requested and DocId check for all endpoints except those excluded
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        try {
            if (auth == null || !auth.isAuthenticated()) {
                writeErrorResponse(response, HttpStatus.UNAUTHORIZED, "Unauthorized");
                return false;
            }

            String userId = UserContext.getUserId();
            String documentId = request.getHeader("X-Document-Id");
            String action = request.getHeader("X-Action-Requested");
            String requestUri = request.getRequestURI();

            // Validate missing parameters
            if (documentId == null || action == null) {
                writeErrorResponse(response, HttpStatus.BAD_REQUEST, "Missing documentId or actionRequested");
                return false;
            }

            // Validate documentId format (000-000)
            if (!documentId.matches(docIdApiMappingProperties.getValidDocIdFormatRegexp())) {
                writeErrorResponse(response, HttpStatus.BAD_REQUEST, "Invalid documentId format. Must be in format: 000-000");
                return false;
            }

//             Validate actionRequested using Annotation
//            if (handler instanceof HandlerMethod handlerMethod) {
//                AllowedAction allowed = handlerMethod.getMethodAnnotation(AllowedAction.class);
//                if (allowed != null) {
//                    String validAction = allowed.value().name();
//                    if (!validAction.equalsIgnoreCase(action)) {
//                        writeErrorResponse(response, HttpStatus.FORBIDDEN,
//                                "Invalid actionRequested for this API. Expected: " + validAction);
//                        return false;
//                    }
//                }
//            }

            AntPathMatcher pathMatcher = new AntPathMatcher();
            
            // Special handling for special endpoints
            boolean isSpecialEndpoint = docIdApiMappingProperties.getSpecial().stream()
                    .anyMatch(pattern -> pathMatcher.match(pattern, requestUri));
            
            if (isSpecialEndpoint) {

                // For searchable-fields, only VIEW action is allowed
                if (!UserRolesRightsEnum.VIEW.toString().equals(action) ) {
                    writeErrorResponse(response, HttpStatus.FORBIDDEN, "Only VIEW action is allowed for searchable fields");
                    return false;
                }
                
                // Validate that the docId exists in any of the mappings
                boolean validDocId = docIdApiMappingProperties.getMappings().values()
                        .stream()
                        .anyMatch(docId -> docId.equals(documentId));
                        
                if (!validDocId) {
                    writeErrorResponse(response, HttpStatus.BAD_REQUEST, "DocumentId doesn't exist.");
                    return false;
                }
            } else {
                // For all other endpoints, validate docId matches the API mapping
                boolean matched = false;
                for (Map.Entry<String, String> entry : docIdApiMappingProperties.getMappings().entrySet()) {
                    if (pathMatcher.match(entry.getKey(), requestUri)) {
                        matched = true;
                        if (!entry.getValue().equals(documentId)) {
                            writeErrorResponse(response, HttpStatus.BAD_REQUEST, "Invalid documentId for this API");
                            return false;
                        }
                        break;
                    }
                }
                
                if (!matched) {
                    writeErrorResponse(response, HttpStatus.NOT_FOUND, "No API mapping found for this endpoint");
                    return false;
                }
            }

            // Validate user permissions
            if (!permissionService.hasPermission(userId, documentId, action)) {
                writeErrorResponse(response, HttpStatus.FORBIDDEN, "You do not have permission to perform this action for this document");
                return false;
            }

            return true; // allow
        } catch (Exception e) {
            writeErrorResponse(response, HttpStatus.INTERNAL_SERVER_ERROR, "Unable to validate user permissions: " + e.getMessage());
            return false;
        }
    }

    private void writeErrorResponse(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType("application/json");
        Map<String, Object> body = Map.of(
                "success", false,
                "statusCode", status.value(),
                "message", message
        );
        objectMapper.writeValue(response.getWriter(), body);
    }
}