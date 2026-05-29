package com.noplay.similia.place_recommend.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class TextRecommendationRequestDto {

    @NotBlank
    private String text;

    private int limit = 10;
}
