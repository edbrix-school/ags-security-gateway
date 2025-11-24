package com.als.securityserver.filter;

import com.als.securityserver.model.AuthenticationDetails;
import com.als.securityserver.util.JwtUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;

    public JwtAuthFilter(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            try {
                final String requestURL = request.getRequestURL().toString();
                final String isRefreshToken = request.getHeader("isRefreshToken");
                if (null != isRefreshToken && "true".equalsIgnoreCase(isRefreshToken)
                        && requestURL.contains("refresh-token")) {
                    if (StringUtils.hasText(token) && jwtUtils.validateRefreshToken(token)) {
                        UsernamePasswordAuthenticationToken auth =
                                new UsernamePasswordAuthenticationToken(null, null, null);
                        SecurityContextHolder.getContext().setAuthentication(auth);
                        request.setAttribute("TOKEN_USER_ID", jwtUtils.getUserIdFromRefreshToken(token));
                    }
                } else {
                    if (jwtUtils.isTokenValid(token)) {
                        List<SimpleGrantedAuthority> authorities = jwtUtils.getRolesFromToken(token).stream()
                                .map(SimpleGrantedAuthority::new)
                                .collect(Collectors.toList());

                        UsernamePasswordAuthenticationToken auth =
                                new UsernamePasswordAuthenticationToken(jwtUtils.getUserIdFromToken(token), null, authorities);
                        auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                        AuthenticationDetails authenticationDetails = new AuthenticationDetails();
                        authenticationDetails.setLoggedInUserEmail(jwtUtils.getUserEmailFromToken(token));
                        authenticationDetails.setLoggedInUserName(jwtUtils.getUserNameFromToken(token));
                        authenticationDetails.setLoggedInUserId(jwtUtils.getUserIdFromToken(token));
                        authenticationDetails.setLoggedInUserPoid(jwtUtils.getUserPoidFromToken(token));
                        authenticationDetails.setLoggedInUserRole(authorities.stream()
                                .findFirst()
                                .map(SimpleGrantedAuthority::getAuthority)
                                .orElse(null));
                        auth.setDetails(authenticationDetails);
                        SecurityContextHolder.getContext().setAuthentication(auth);
                    } else {
                        throw new JwtException("Invalid Token");
                    }
                }
            } catch (ExpiredJwtException e) {
                sendErrorResponse(response, "Token has expired", HttpServletResponse.SC_UNAUTHORIZED);
                return;
            } catch (JwtException | IllegalArgumentException e) {
                sendErrorResponse(response, "Invalid Authorization Token", HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private void sendErrorResponse(HttpServletResponse response, String message, int status) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        new ObjectMapper().writeValue(response.getWriter(), Map.of(
                "success", false,
                "code", status,
                "error", message
        ));
    }
}

