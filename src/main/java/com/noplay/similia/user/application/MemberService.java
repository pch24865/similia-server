package com.noplay.similia.user.application;

import com.noplay.similia.global.exception.BusinessException;
import com.noplay.similia.global.exception.ErrorCode;
import com.noplay.similia.user.domain.Member;
import com.noplay.similia.user.infrastructure.MemberRepository;
import com.noplay.similia.user.api.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Transactional
    public MemberResponseDto signUp(SignUpRequestDto dto) {
        if (memberRepository.existsByEmail(dto.getEmail())) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }

        Member member = Member.builder()
                .email(dto.getEmail())
                .nickname(dto.getNickname())
                .name(dto.getName())
                .password(passwordEncoder.encode(dto.getPassword()))
                .phone(dto.getPhone())
                .build();

        return MemberResponseDto.from(memberRepository.save(member));
    }

    // [제거됨] login() 은 AuthService.login()에서 토큰 발급과 함께 처리합니다.
    // MemberService에 동일한 메서드가 존재하면 혼란을 줄 수 있어 삭제합니다.

    public MemberResponseDto getMember(Long id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        return MemberResponseDto.from(member);
    }

    @Transactional
    public MemberResponseDto update(Long id, UpdateMemberRequestDto dto) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        member.update(dto.getNickname(), dto.getName(), dto.getPhone());

        return MemberResponseDto.from(member);
    }

    @Transactional
    public void changePassword(Long memberId, ChangePasswordRequestDto dto) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        if (!passwordEncoder.matches(dto.getCurrentPassword(), member.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        if (!dto.getNewPassword().equals(dto.getConfirmNewPassword())) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        member.changePassword(passwordEncoder.encode(dto.getNewPassword()));
    }

    @Transactional
    public void deleteMember(Long id) {
        // INVALID_INPUT_VALUE가 아닌 MEMBER_NOT_FOUND로 수정 - 존재하지 않는 회원 탈퇴 시도에 맞는 에러코드
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        memberRepository.delete(member);
    }
}