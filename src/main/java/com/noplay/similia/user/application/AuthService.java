package com.noplay.similia.user.application;

import com.noplay.similia.global.security.JwtProvider;
import com.noplay.similia.user.api.dto.LoginRequestDto;
import com.noplay.similia.user.api.dto.TokenResponseDto;
import com.noplay.similia.user.domain.Member;
import com.noplay.similia.user.domain.RefreshToken;
import com.noplay.similia.user.domain.RefreshTokenRepository;
import com.noplay.similia.user.infrastructure.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public TokenResponseDto login(LoginRequestDto dto) {
        Member member = memberRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다."));

        if (!passwordEncoder.matches(dto.getPassword(), member.getPassword())) {
            throw new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        String accessToken = jwtProvider.createAccessToken(member.getId());
        String refreshTokenString = jwtProvider.createRefreshToken(member.getId());

        // Refresh Token DB 저장 (기존에 있으면 업데이트, 없으면 생성)
        LocalDateTime expiryDate = LocalDateTime.now().plusDays(7); // 7일 (JwtProvider 의 설정값과 맞춤)
        RefreshToken refreshToken = refreshTokenRepository.findByMemberId(member.getId())
                .orElse(RefreshToken.builder()
                        .memberId(member.getId())
                        .token(refreshTokenString)
                        .expiryDate(expiryDate)
                        .build());
        
        refreshToken.updateToken(refreshTokenString, expiryDate);
        refreshTokenRepository.save(refreshToken);

        return new TokenResponseDto(accessToken, refreshTokenString);
    }

    @Transactional
    public void logout(Long memberId) {
        refreshTokenRepository.deleteByMemberId(memberId);
    }
}
