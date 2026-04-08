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

    public MemberResponseDto login(LoginRequestDto dto) {
        Member member = memberRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(dto.getPassword(), member.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        return MemberResponseDto.from(member);
    }

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
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT_VALUE));

        memberRepository.delete(member);
    }
}