package com.asg.security.gateway.util;

import com.asg.security.gateway.config.JwtConfig;
import com.asg.security.gateway.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Component
public class JwtUtils {

    private final JwtConfig jwtConfig;

    public JwtUtils(JwtConfig jwtConfig) {
        this.jwtConfig = jwtConfig;
    }

    public String generateToken(User user, List<String> roleNames) {
        long now = System.currentTimeMillis();
try {
    return Jwts.builder()
            .setSubject(user.getUserId())
            .claim("email", user.getEmail())
            .claim("userName", user.getUserName())
            .claim("userPoid", user.getUserPoid())
            .claim("groupPoid", user.getGroupPoid())
            .claim("companyPoid", user.getDefaultCompanyPoid())
            .claim("roles", roleNames)
            .setIssuedAt(new Date(now))
            .setExpiration(new Date(now + jwtConfig.getExpirationInMs()))
            .signWith(SignatureAlgorithm.HS256, jwtConfig.getSecret())
            .compact();
}catch (Exception e){
    e.printStackTrace();
    throw new RuntimeException(e);
}
    }

    public String generateRefreshToken(String userId) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setSubject(userId)
                .setExpiration(new Date(now + jwtConfig.getRefreshExpirationInMs()))
                .signWith(SignatureAlgorithm.HS256, jwtConfig.getRefreshSecret())
                .compact();
    }

    public boolean isTokenValid(String token) {
        try {
            Jwts.parser().setSigningKey(jwtConfig.getSecret()).parseClaimsJws(token);
            return true;
        } catch (SignatureException | ExpiredJwtException ex) {
            throw ex;
        } catch (Exception ex) {
            return false;
        }
    }

    public boolean validateRefreshToken(String token) {
        try {
            Jwts.parser().setSigningKey(jwtConfig.getRefreshSecret()).parseClaimsJws(token);
            return true;
        } catch (SignatureException | ExpiredJwtException ex) {
            throw ex;
        }
    }

    public String getUserIdFromToken(String token) {
        return getClaims(token, jwtConfig.getSecret()).getSubject();
    }

    public String getUserIdFromRefreshToken(String token) {
        return getClaims(token, jwtConfig.getRefreshSecret()).getSubject();
    }

    public List<String> getRolesFromToken(String token) {
        Claims claims = getClaims(token, jwtConfig.getSecret());
        Object value = claims.get("roles");
        if (value instanceof List<?> raw) {
            List<String> roles = new ArrayList<>();
            for (Object item : raw) {
                roles.add(String.valueOf(item));
            }
            return roles;
        }
        return List.of();
    }

    public String getUserNameFromToken(String token) {
        return getClaims(token, jwtConfig.getSecret()).get("userName", String.class);
    }

    public String getUserEmailFromToken(String token) {
        return getClaims(token, jwtConfig.getSecret()).get("email", String.class);
    }

    public Long getUserPoidFromToken(String token) {
        return getClaims(token, jwtConfig.getSecret()).get("userPoid", Long.class);
    }

    public Long getGroupPoidFromToken(String token) {
        return getClaims(token, jwtConfig.getSecret()).get("groupPoid", Long.class);
    }

    public Long getCompanyPoidFromToken(String token) {
        return getClaims(token, jwtConfig.getSecret()).get("companyPoid", Long.class);
    }

    public Date getExpirationDate(String token) {
        return getClaims(token, jwtConfig.getSecret()).getExpiration();
    }

    private Claims getClaims(String token, String signingKey) {
        return Jwts.parser()
                .setSigningKey(signingKey)
                .parseClaimsJws(token)
                .getBody();
    }
}

