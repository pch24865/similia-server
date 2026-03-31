package com.noplay.similia.user.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateMemberRequestDto {

    @NotBlank(message = "닉네임은 필수입니다.")
    @Size(min = 2, max = 20, message = "닉네임은 2자 이상 20자 이하로 입력해주세요.")
    @Pattern(
            regexp = "^(?!\\s)(?!.*\\s$).{2,20}$",
            message = "닉네임은 앞뒤 공백 없이 입력해주세요."
    )
    private String nickname;

    @NotBlank(message = "이름은 필수입니다.")
    @Size(min = 2, max = 50, message = "이름은 2자 이상 50자 이하로 입력해주세요.")
    @Pattern(
            regexp = "^(?!\\s)(?!.*\\s$).{2,50}$",
            message = "이름은 앞뒤 공백 없이 입력해주세요."
    )
    private String name;

    @NotBlank(message = "전화번호는 필수입니다.")
    @Pattern(
            regexp = "^01[016789]-?\\d{3,4}-?\\d{4}$",
            message = "전화번호 형식이 올바르지 않습니다."
    )
    @Size(max = 20, message = "전화번호는 20자 이하여야 합니다.")
    private String phone;
}