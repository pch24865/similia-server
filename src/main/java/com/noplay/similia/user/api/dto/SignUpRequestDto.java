package com.noplay.similia.user.api.dto;

import com.noplay.similia.user.api.validation.FieldMatch;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@FieldMatch(
        first = "password",
        second = "confirmPassword",
        message = "비밀번호와 비밀번호 확인이 일치하지 않습니다."
)
public class SignUpRequestDto {

    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    @Size(max = 100, message = "이메일은 100자 이하여야 합니다.")
    @Pattern(regexp = "^\\S+$", message = "이메일에는 공백을 포함할 수 없습니다.")
    private String email;

    @NotBlank(message = "닉네임은 필수입니다.")
    @Size(min = 2, max = 20, message = "닉네임은 2자 이상 20자 이하로 입력해주세요.")
    @Pattern(
            regexp = "^(?!\\s)(?!.*\\s$).{2,20}$",
            message = "닉네임은 앞뒤 공백 없이 2자 이상 20자 이하로 입력해주세요."
    )
    private String nickname;

    @NotBlank(message = "이름은 필수입니다.")
    @Size(min = 2, max = 50, message = "이름은 2자 이상 50자 이하로 입력해주세요.")
    @Pattern(
            regexp = "^(?!\\s)(?!.*\\s$).{2,50}$",
            message = "이름은 앞뒤 공백 없이 입력해주세요."
    )
    private String name;

    @NotBlank(message = "비밀번호는 필수입니다.")
    @Size(min = 8, max = 20, message = "비밀번호는 8자 이상 20자 이하로 입력해주세요.")
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-={}\\[\\]:;\"'<>,.?/\\\\|`~])\\S{8,20}$",
            message = "비밀번호는 영문, 숫자, 특수문자를 각각 1개 이상 포함해야 하며 공백 없이 8자 이상 20자 이하로 입력해주세요."
    )
    private String password;

    @NotBlank(message = "비밀번호 확인은 필수입니다.")
    @Size(min = 8, max = 20, message = "비밀번호 확인은 8자 이상 20자 이하로 입력해주세요.")
    @Pattern(regexp = "^\\S+$", message = "비밀번호 확인에는 공백을 포함할 수 없습니다.")
    private String confirmPassword;

    @NotBlank(message = "전화번호는 필수입니다.")
    @Pattern(
            regexp = "^01[016789]-?\\d{3,4}-?\\d{4}$",
            message = "전화번호 형식이 올바르지 않습니다."
    )
    @Size(max = 20, message = "전화번호는 20자 이하여야 합니다.")
    private String phone;
}