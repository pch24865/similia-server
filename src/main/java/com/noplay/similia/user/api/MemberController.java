package com.noplay.similia.user.api;

import com.noplay.similia.global.exception.BusinessException;
import com.noplay.similia.global.exception.ErrorCode;
import com.noplay.similia.user.api.dto.ChangePasswordRequestDto;
import com.noplay.similia.user.api.dto.MemberResponseDto;
import com.noplay.similia.user.api.dto.SignUpRequestDto;
import com.noplay.similia.user.api.dto.UpdateMemberRequestDto;
import com.noplay.similia.user.application.MemberService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Member", description = "회원 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/members")
public class MemberController {

    private final MemberService memberService;

    // 회원가입
    @PostMapping
    public MemberResponseDto signUp(@Valid @RequestBody SignUpRequestDto dto) {
        return memberService.signUp(dto);
    }

    // 내 정보 조회
    @GetMapping("/profile")
    public MemberResponseDto getMyProfile(@AuthenticationPrincipal String memberId) {
        Long loginMemberId = parseMemberId(memberId);
        return memberService.getMember(loginMemberId);
    }

    // 내 정보 수정
    @PatchMapping("/profile")
    public MemberResponseDto updateMyProfile(
            @Valid @RequestBody UpdateMemberRequestDto dto,
            @AuthenticationPrincipal String memberId
    ) {
        Long loginMemberId = parseMemberId(memberId);
        return memberService.update(loginMemberId, dto);
    }

    // 비밀번호 변경
    @PatchMapping("/password")
    public String changePassword(
            @Valid @RequestBody ChangePasswordRequestDto dto,
            @AuthenticationPrincipal String memberId
    ) {
        Long loginMemberId = parseMemberId(memberId);
        memberService.changePassword(loginMemberId, dto);
        return "비밀번호가 변경되었습니다.";
    }

    // 회원탈퇴
    @DeleteMapping("/profile")
    public String deleteMyProfile(@AuthenticationPrincipal String memberId) {
        Long loginMemberId = parseMemberId(memberId);
        memberService.deleteMember(loginMemberId);
        return "회원탈퇴가 완료되었습니다.";
    }

    /**
     * JWT에서 추출한 memberId(String)를 Long으로 변환합니다.
     * - memberId가 null이면 토큰이 없거나 만료된 것이므로 401(INVALID_TOKEN) 반환
     * - IllegalArgumentException 대신 BusinessException을 사용하여
     *   GlobalExceptionHandler가 일관된 에러 형식으로 처리할 수 있게 합니다.
     */
    private Long parseMemberId(String memberId) {
        if (memberId == null) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }
        return Long.parseLong(memberId);
    }
}