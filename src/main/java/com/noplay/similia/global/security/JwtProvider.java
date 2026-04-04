package com.noplay.similia.global.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Collections;
import java.util.Date;

@Component
public class JwtProvider {

    private final Key key;
    private final long accessTokenValidityTime;
    private final long refreshTokenValidityTime;

    public JwtProvider(
            @Value("${jwt.secret:c2ltaWxpYS1zZWNyZXQta2V5LXRlc3QtdmFsdWUtc2ltaWxpYS1zZWNyZXQta2V5LXRlc3QtdmFsdWU=}") String secretKey,
            @Value("${jwt.access-token-validity-in-milliseconds:1800000}") long accessTokenValidityTime, // 30 minutes
            @Value("${jwt.refresh-token-validity-in-milliseconds:604800000}") long refreshTokenValidityTime) { // 7 days
        byte[] keyBytes = io.jsonwebtoken.io.Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.accessTokenValidityTime = accessTokenValidityTime;
        this.refreshTokenValidityTime = refreshTokenValidityTime;
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
        // 실제 운영에서는 UserDetailsService를 통해 Member 엔티티를 로드하여 UserDetails 객체를 만드는 것이 정석이나,
        // 성능과 편의를 위해 memberId 자체를 Principal로 사용합니다.
        return new UsernamePasswordAuthenticationToken(memberId, token, Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            // 이외의 예외: 만료, 잘못된 서명 등 처리 (실제로는 더 세밀한 예외 처리가 필요함)
            return false;
        }
    }
}
