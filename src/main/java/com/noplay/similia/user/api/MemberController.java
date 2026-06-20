package com.noplay.similia.user.api;

import com.noplay.similia.global.exception.BusinessException;
import com.noplay.similia.global.exception.ErrorCode;
import com.noplay.similia.global.security.CustomUserDetails;
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
    public MemberResponseDto getMyProfile(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Long loginMemberId = getMemberId(userDetails);
        return memberService.getMember(loginMemberId);
    }

    // 내 정보 수정
    @PatchMapping("/profile")
    public MemberResponseDto updateMyProfile(
            @Valid @RequestBody UpdateMemberRequestDto dto,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long loginMemberId = getMemberId(userDetails);
        return memberService.update(loginMemberId, dto);
    }

    // 비밀번호 변경
    @PatchMapping("/password")
    public String changePassword(
            @Valid @RequestBody ChangePasswordRequestDto dto,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long loginMemberId = getMemberId(userDetails);
        memberService.changePassword(loginMemberId, dto);
        return "비밀번호가 변경되었습니다.";
    }

    // 회원탈퇴
    @DeleteMapping("/profile")
    public String deleteMyProfile(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Long loginMemberId = getMemberId(userDetails);
        memberService.deleteMember(loginMemberId);
        return "회원탈퇴가 완료되었습니다.";
    }

    private Long getMemberId(CustomUserDetails userDetails) {
        if (userDetails == null || userDetails.getMember() == null) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }
        return userDetails.getMember().getId();
    }
}