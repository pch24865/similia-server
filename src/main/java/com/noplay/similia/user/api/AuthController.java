package com.noplay.similia.user.api;

import com.noplay.similia.global.exception.BusinessException;
import com.noplay.similia.global.exception.ErrorCode;
import com.noplay.similia.global.security.CustomUserDetails;
import com.noplay.similia.user.api.dto.LoginRequestDto;
import com.noplay.similia.user.api.dto.TokenResponseDto;
import com.noplay.similia.user.application.AuthService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Auth", description = "인증(토큰 발급/삭제) API - RESTful")
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth/tokens")
public class AuthController {

    private final AuthService authService;

    // 토큰 생성 = 로그인
    @PostMapping
    public ResponseEntity<TokenResponseDto> createToken(
            @Valid @RequestBody LoginRequestDto dto,
            HttpServletResponse response
    ) {
        TokenResponseDto tokenDto = authService.login(dto);

        // [Refresh Token 위치]: 클라이언트가 직접 접근할 수 없도록 HttpOnly 속성의 쿠키(Cookie)에 담겨 전달됩니다.
        Cookie refreshTokenCookie = new Cookie("refresh_token", tokenDto.getRefreshToken());
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(true); // HTTPS 적용 시 활성화
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(7 * 24 * 60 * 60); // 7일
        response.addCookie(refreshTokenCookie);

        // 자원을 생성했으므로 REST 적으로는 201 Created 가 적절하지만
        // Token 통신의 관례상 200 OK 도 자주 쓰이므로 선택 가능합니다. (여기서는 201을 씁니다)
        return ResponseEntity.status(HttpStatus.CREATED).body(tokenDto);
    }

    // 토큰 갱신 = 토큰 재발급
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponseDto> refreshToken(
            // [Refresh Token 위치]: 클라이언트의 요청 헤더 내 쿠키(Cookie)에서 자동으로 추출됩니다.
            @CookieValue(value = "refresh_token", required = false) String refreshToken,
            HttpServletResponse response
    ) {
        if (refreshToken == null) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        TokenResponseDto tokenDto = authService.reissue(refreshToken);

        // 새로운 Refresh Token 쿠키 발급
        Cookie newRefreshTokenCookie = new Cookie("refresh_token", tokenDto.getRefreshToken());
        newRefreshTokenCookie.setHttpOnly(true);
        newRefreshTokenCookie.setSecure(true); // HTTPS 적용 시 활성화
        newRefreshTokenCookie.setPath("/");
        newRefreshTokenCookie.setMaxAge(7 * 24 * 60 * 60); // 7일
        response.addCookie(newRefreshTokenCookie);

        return ResponseEntity.ok(tokenDto);
    }

    // 토큰 삭제 = 로그아웃
    @DeleteMapping
    public ResponseEntity<Void> deleteToken(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletResponse response
    ) {
        if (userDetails != null && userDetails.getMember() != null) {
            authService.logout(userDetails.getMember().getId());
        }

        // [Refresh Token 위치]: 저장되어 있던 쿠키(Cookie)의 수명을 0으로 만들어 삭제를 유도합니다.
        Cookie refreshTokenCookie = new Cookie("refresh_token", null);
        refreshTokenCookie.setMaxAge(0);
        refreshTokenCookie.setPath("/");
        response.addCookie(refreshTokenCookie);

        // REST 원칙에 따라 리소스 삭제 후 반환할 응답 Body가 없으므로 204 No Content 권장
        return ResponseEntity.noContent().build();
    }
}
