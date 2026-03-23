package com.noplay.similia.dto;

import com.noplay.similia.domain.Member;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class MemberResponseDto {

    private Long id;
    private String email;
    private String nickname;
    private String name;
    private String phone;
    private LocalDateTime createdAt;

    public static MemberResponseDto from(Member member) {
        return MemberResponseDto.builder()
                .id(member.getId())
                .email(member.getEmail())
                .nickname(member.getNickname())
                .name(member.getName())
                .phone(member.getPhone())
                .createdAt(member.getCreatedAt())
                .build();
    }
}