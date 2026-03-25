package com.noplay.similia.controller;

import com.noplay.similia.dto.LoginRequestDto;
import com.noplay.similia.dto.MemberResponseDto;
import com.noplay.similia.service.MemberService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/sessions")
public class SessionController {

    private final MemberService memberService;

    // 로그인 = 세션 생성
    @PostMapping
    public MemberResponseDto login(
            @Valid @RequestBody LoginRequestDto dto,
            HttpSession session
    ) {
        MemberResponseDto loginMember = memberService.login(dto);
        session.setAttribute("loginMemberId", loginMember.getId());
        return loginMember;
    }

    // 로그아웃 = 세션 삭제
    @DeleteMapping
    public String logout(HttpSession session) {
        session.invalidate();
        return "로그아웃 완료";
    }
}