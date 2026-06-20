package com.noplay.similia.global.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Collections;
import java.util.Date;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class JwtProvider {

    private final Key key;
    private final long accessTokenValidityTime;
    private final long refreshTokenValidityTime;
    private final CustomUserDetailsService customUserDetailsService;

    public JwtProvider(
            @Value("${jwt.secret:c2ltaWxpYS1zZWNyZXQta2V5LXRlc3QtdmFsdWUtc2ltaWxpYS1zZWNyZXQta2V5LXRlc3QtdmFsdWU=}") String secretKey,
            @Value("${jwt.access-token-validity-in-milliseconds:1800000}") long accessTokenValidityTime, // 30 minutes
            @Value("${jwt.refresh-token-validity-in-milliseconds:604800000}") long refreshTokenValidityTime, // 7 days
            CustomUserDetailsService customUserDetailsService) { 
        byte[] keyBytes = io.jsonwebtoken.io.Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.accessTokenValidityTime = accessTokenValidityTime;
        this.refreshTokenValidityTime = refreshTokenValidityTime;
        this.customUserDetailsService = customUserDetailsService;
    }

    public String createAccessToken(Long memberId) {
        return createToken(memberId, accessTokenValidityTime);
    }

    public String createRefreshToken(Long memberId) {
        return createToken(memberId, refreshTokenValidityTime);
    }

    private String createToken(Long memberId, long validityTime) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + validityTime);

        return Jwts.builder()
                .setSubject(String.valueOf(memberId))
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Authentication getAuthentication(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        String memberId = claims.getSubject();
        
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(memberId);
        return new UsernamePasswordAuthenticationToken(userDetails, token, userDetails.getAuthorities());
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.warn("잘못된 JWT 서명입니다: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.warn("만료된 JWT 토큰입니다: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.warn("지원되지 않는 JWT 토큰입니다: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("JWT 토큰이 잘못되었습니다: {}", e.getMessage());
        }
        return false;
    }
}
