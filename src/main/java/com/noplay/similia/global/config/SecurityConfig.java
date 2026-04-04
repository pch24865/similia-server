package com.noplay.similia.global.config;

import com.noplay.similia.global.security.JwtAuthenticationFilter;
import com.noplay.similia.global.security.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtProvider jwtProvider;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .httpBasic(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sessionManagement ->
                        sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .headers(headers -> 
                        headers.frameOptions(frameOptions -> frameOptions.sameOrigin())
                )
                .authorizeHttpRequests(authorizeRequests ->
                        authorizeRequests
                                // 정적 파일 및 뷰 페이지 허용
                                .requestMatchers("/", "/*.html", "/css/**", "/js/**", "/images/**", "/favicon.ico", "/error").permitAll()
                                // H2 콘솔 허용
                                .requestMatchers("/h2-console/**").permitAll()
                                // API 허용
                                .requestMatchers("/members", "/auth/**", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                                .anyRequest().authenticated()
                )
                .addFilterBefore(new JwtAuthenticationFilter(jwtProvider), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
