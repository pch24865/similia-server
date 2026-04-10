package com.noplay.similia.user.application;

import com.noplay.similia.global.exception.BusinessException;
import com.noplay.similia.global.exception.ErrorCode;
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
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(dto.getPassword(), member.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
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

    @Transactional
    public TokenResponseDto reissue(String refreshTokenString) {
        // 1. 전달받은 Refresh Token의 유효성 검증
        if (!jwtProvider.validateToken(refreshTokenString)) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        // 2. 토큰에서 memberId 추출 (JwtProvider에서 Authentication 객체 생성 시 memberId가 Principal로 들어감)
        String memberIdStr = jwtProvider.getAuthentication(refreshTokenString).getName();
        Long memberId = Long.parseLong(memberIdStr);

        // 3. DB에 저장된 Refresh Token과 일치하는지 확인
        RefreshToken refreshToken = refreshTokenRepository.findByMemberId(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_TOKEN));

        if (!refreshToken.getToken().equals(refreshTokenString)) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        // 4. 새로운 토큰 생성
        String newAccessToken = jwtProvider.createAccessToken(memberId);
        String newRefreshTokenString = jwtProvider.createRefreshToken(memberId);

        // 5. DB 업데이트
        LocalDateTime expiryDate = LocalDateTime.now().plusDays(7);
        refreshToken.updateToken(newRefreshTokenString, expiryDate);

        return new TokenResponseDto(newAccessToken, newRefreshTokenString);
    }
}
