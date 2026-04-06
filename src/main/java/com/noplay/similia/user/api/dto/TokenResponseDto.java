package com.noplay.similia.user.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TokenResponseDto {
    private String accessToken;
    // 편의상 Body로도 주지만, 실제로는 쿠키에 담는 것이 권장됩니다.
    // AuthController에서 쿠키 처리를 병행할 예정입니다.
    private String refreshToken; 
}
