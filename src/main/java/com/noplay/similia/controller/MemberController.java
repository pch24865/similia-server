package com.noplay.similia.controller;

import com.noplay.similia.dto.*;
import com.noplay.similia.service.MemberService;
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
    @PostMapping("/signup")
    public MemberResponseDto signUp(@Valid @RequestBody SignUpRequestDto dto) {
        return memberService.signUp(dto);
    }

    // 로그인
    @PostMapping("/login")
    public MemberResponseDto login(@Valid @RequestBody LoginRequestDto dto, HttpSession session) {
        MemberResponseDto loginMember = memberService.login(dto);
        session.setAttribute("loginMemberId", loginMember.getId());
        return loginMember;
    }

    // 로그아웃
    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "로그아웃 완료";
    }

    // 내 정보 조회
    @GetMapping("/me")
    public MemberResponseDto getMyInfo(HttpSession session) {
        Long loginMemberId = (Long) session.getAttribute("loginMemberId");

        if (loginMemberId == null) {
            throw new IllegalArgumentException("로그인이 필요합니다.");
        }

        return memberService.getMember(loginMemberId);
    }

    // 내 정보 수정
    @PutMapping("/me")
    public MemberResponseDto updateMyInfo(
            @Valid @RequestBody UpdateMemberRequestDto dto,
            HttpSession session
    ) {
        Long loginMemberId = (Long) session.getAttribute("loginMemberId");

        if (loginMemberId == null) {
            throw new IllegalArgumentException("로그인이 필요합니다.");
        }

        return memberService.update(loginMemberId, dto);
    }

    @PutMapping("/me/password")
    public String changeMyPassword(
            @Valid @RequestBody ChangePasswordRequestDto dto,
            HttpSession session
    ) {
        Long loginMemberId = (Long) session.getAttribute("loginMemberId");

        if (loginMemberId == null) {
            throw new IllegalArgumentException("로그인이 필요합니다.");
        }

        memberService.changePassword(loginMemberId, dto);
        return "비밀번호가 변경되었습니다.";
    }

    @DeleteMapping("/me")
    public String deleteMyAccount(HttpSession session) {
        Long loginMemberId = (Long) session.getAttribute("loginMemberId");

        if (loginMemberId == null) {
            throw new IllegalArgumentException("로그인이 필요합니다.");
        }

        memberService.deleteMember(loginMemberId);
        session.invalidate();

        return "회원탈퇴가 완료되었습니다.";
    }
}