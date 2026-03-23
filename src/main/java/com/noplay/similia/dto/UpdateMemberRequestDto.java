package com.noplay.similia.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateMemberRequestDto {

    @NotBlank
    private String nickname;

    @NotBlank
    private String name;

    @NotBlank
    @Pattern(regexp = "^01[016789]-?\\d{3,4}-?\\d{4}$")
    private String phone;
}