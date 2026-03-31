package com.noplay.similia.user.api.dto;

import com.noplay.similia.user.api.validation.DifferentFields;
import com.noplay.similia.user.api.validation.FieldMatch;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@FieldMatch(
        first = "newPassword",
        second = "confirmNewPassword",
        message = "새 비밀번호와 새 비밀번호 확인이 일치하지 않습니다."
)
@DifferentFields(
        first = "currentPassword",
        second = "newPassword",
        message = "현재 비밀번호와 새 비밀번호는 같을 수 없습니다."
)
public class ChangePasswordRequestDto {

    @NotBlank(message = "현재 비밀번호는 필수입니다.")
    @Size(min = 8, max = 20, message = "현재 비밀번호는 8자 이상 20자 이하로 입력해주세요.")
    @Pattern(regexp = "^\\S+$", message = "현재 비밀번호에는 공백을 포함할 수 없습니다.")
    private String currentPassword;

    @NotBlank(message = "새 비밀번호는 필수입니다.")
    @Size(min = 8, max = 20, message = "새 비밀번호는 8자 이상 20자 이하로 입력해주세요.")
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-={}\\[\\]:;\"'<>,.?/\\\\|`~])\\S{8,20}$",
            message = "새 비밀번호는 영문, 숫자, 특수문자를 각각 1개 이상 포함해야 하며 공백 없이 8자 이상 20자 이하로 입력해주세요."
    )
    private String newPassword;

    @NotBlank(message = "새 비밀번호 확인은 필수입니다.")
    @Size(min = 8, max = 20, message = "새 비밀번호 확인은 8자 이상 20자 이하로 입력해주세요.")
    @Pattern(regexp = "^\\S+$", message = "새 비밀번호 확인에는 공백을 포함할 수 없습니다.")
    private String confirmNewPassword;
}