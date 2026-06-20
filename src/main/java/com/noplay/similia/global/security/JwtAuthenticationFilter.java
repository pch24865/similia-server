package com.noplay.similia.global.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 헤더에서 JWT 추출
        String token = resolveToken(request);

        // JWT 유효성 검사
        if (token != null && jwtProvider.validateToken(token)) {
            try {
                // 토큰이 유효할 경우 토큰에서 Authentication 객체를 가지고 와서 SecurityContext에 저장
                Authentication authentication = jwtProvider.getAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (Exception e) {
                // 탈퇴한 유저의 유효한 토큰 등으로 인해 UserDetailsService에서 예외 발생 시
                // 인증 객체를 저장하지 않아 미인증 상태로 처리 → authenticationEntryPoint가 401 반환
                log.warn("유효한 토큰이지만 인증 객체 생성에 실패했습니다: {}", e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }

    // Request Header에서 토큰 정보 추출
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}

