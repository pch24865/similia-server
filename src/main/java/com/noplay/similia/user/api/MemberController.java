package com.noplay.similia.user.api;

import com.noplay.similia.user.api.dto.ChangePasswordRequestDto;
import com.noplay.similia.user.api.dto.MemberResponseDto;
import com.noplay.similia.user.api.dto.SignUpRequestDto;
import com.noplay.similia.user.api.dto.UpdateMemberRequestDto;
import com.noplay.similia.user.application.MemberService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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
    public MemberResponseDto getMyProfile(HttpSession session) {
        Long loginMemberId = getLoginMemberId(session);
        return memberService.getMember(loginMemberId);
    }

    // 내 정보 수정
    @PatchMapping("/profile")
    public MemberResponseDto updateMyProfile(
            @Valid @RequestBody UpdateMemberRequestDto dto,
            HttpSession session
    ) {
        Long loginMemberId = getLoginMemberId(session);
        return memberService.update(loginMemberId, dto);
    }

    // 비밀번호 변경
    @PatchMapping("/password")
    public String changePassword(
            @Valid @RequestBody ChangePasswordRequestDto dto,
            HttpSession session
    ) {
        Long loginMemberId = getLoginMemberId(session);
        memberService.changePassword(loginMemberId, dto);
        return "비밀번호가 변경되었습니다.";
    }

    // 회원탈퇴
    @DeleteMapping("/profile")
    public String deleteMyProfile(HttpSession session) {
        Long loginMemberId = getLoginMemberId(session);
        memberService.deleteMember(loginMemberId);
        session.invalidate();
        return "회원탈퇴가 완료되었습니다.";
    }

    private Long getLoginMemberId(HttpSession session) {
        Long loginMemberId = (Long) session.getAttribute("loginMemberId");
        if (loginMemberId == null) {
            throw new IllegalArgumentException("로그인이 필요합니다.");
        }
        return loginMemberId;
    }
}